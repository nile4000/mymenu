package dev.lueem.integration.supercard.app

import dev.lueem.extraction.app.ExtractionService
import dev.lueem.integration.supercard.api.SupercardSessionRequest
import dev.lueem.integration.supercard.api.SupercardSyncSingleRequest
import dev.lueem.integration.supercard.api.SupercardAvailableResponse
import dev.lueem.integration.supercard.api.SupercardAvailableReceipt
import dev.lueem.integration.supercard.api.SupercardStatusResponse
import dev.lueem.integration.supercard.api.SupercardSyncResponse
import dev.lueem.integration.supercard.infra.CookieCrypto
import dev.lueem.integration.supercard.infra.SupercardConfigStore
import dev.lueem.integration.supercard.infra.SupercardHtmlParser
import dev.lueem.integration.supercard.infra.SupercardHttpClient
import dev.lueem.integration.supercard.infra.SupercardReceiptLink
import dev.lueem.integration.supercard.infra.SupercardReceiptRepository
import dev.lueem.shared.config.SupercardProperties
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.nio.file.Files
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.logging.Level
import java.util.logging.Logger

@ApplicationScoped
class SupercardService @Inject constructor(
    private val configStore: SupercardConfigStore,
    private val cookieCrypto: CookieCrypto,
    private val supercardHttpClient: SupercardHttpClient,
    private val htmlParser: SupercardHtmlParser,
    private val extractionService: ExtractionService,
    private val receiptRepository: SupercardReceiptRepository,
    private val properties: SupercardProperties
) {

    companion object {
        private val LOGGER = Logger.getLogger(SupercardService::class.java.name)
        private const val KEY_COOKIE = "supercard_session_encrypted"
        private const val KEY_NAME = "supercard_name"
        private const val SOURCE = "supercard"
    }

    @Volatile
    private var runtimeCookieEncrypted: String? = null

    @Volatile
    private var runtimeSupercardName: String? = null

    @Volatile
    private var runtimeSessionUpdatedAt: Instant? = null

    fun setSession(request: SupercardSessionRequest): SupercardStatusResponse {
        require(request.cookieHeader.isNotBlank()) { "cookieHeader must not be blank" }
        require(properties.encryptionKey.isNotBlank()) { "SUPERCARD_SESSION_ENC_KEY is required" }
        // Validate login/session against Supercard first.
        supercardHttpClient.fetchReceiptOverviewHtml(request.cookieHeader.trim())

        val encrypted = cookieCrypto.encrypt(request.cookieHeader.trim())
        runtimeCookieEncrypted = encrypted
        runtimeSupercardName = request.supercardName?.trim()
        runtimeSessionUpdatedAt = Instant.now()

        if (!propertiesMissingForDb()) {
            ensureSchema()
            configStore.upsertEncrypted(KEY_COOKIE, encrypted)
            val providedName = request.supercardName
            if (providedName != null) {
                configStore.upsertText(KEY_NAME, providedName.trim())
            }
        }
        return SupercardStatusResponse(
            connected = true,
            supercardName = runtimeSupercardName,
            sessionUpdatedAt = runtimeSessionUpdatedAt?.toString()
        )
    }

    fun getStatus(): SupercardStatusResponse {
        if (propertiesMissingForDb()) {
            return SupercardStatusResponse(
                connected = !runtimeCookieEncrypted.isNullOrBlank(),
                supercardName = runtimeSupercardName,
                sessionUpdatedAt = runtimeSessionUpdatedAt?.toString()
            )
        }
        ensureSchema()
        val encrypted = configStore.getEncrypted(KEY_COOKIE) ?: runtimeCookieEncrypted
        val name = configStore.getText(KEY_NAME) ?: runtimeSupercardName
        val updatedAt = configStore.getUpdatedAt(KEY_COOKIE)?.toString() ?: runtimeSessionUpdatedAt?.toString()

        return SupercardStatusResponse(
            connected = !encrypted.isNullOrBlank(),
            supercardName = name,
            sessionUpdatedAt = updatedAt
        )
    }

    fun sync(): SupercardSyncResponse {
        return syncAvailable()
    }

    fun available(): SupercardAvailableResponse {
        val cookieHeader = resolveCookieHeader()
        val links = parseAvailableLinks(cookieHeader)
        return SupercardAvailableResponse(
            count = links.size,
            receipts = links.map { SupercardAvailableReceipt(it.receiptUrl, it.externalReceiptId) }
        )
    }

    fun syncSingle(request: SupercardSyncSingleRequest): SupercardSyncResponse {
        val cookieHeader = resolveCookieHeader()
        val link = resolveSingleLink(request)
        return syncLinks(cookieHeader, listOf(link))
    }

    fun syncAvailable(): SupercardSyncResponse {
        val cookieHeader = resolveCookieHeader()
        val links = parseAvailableLinks(cookieHeader)
        return syncLinks(cookieHeader, links)
    }

    private fun syncLinks(cookieHeader: String, links: List<SupercardReceiptLink>): SupercardSyncResponse {
        require(properties.encryptionKey.isNotBlank()) { "SUPERCARD_SESSION_ENC_KEY is required" }
        require(!propertiesMissingForDb()) {
            "Missing Supercard DB config. Set SUPERCARD_DB_URL, SUPERCARD_DB_USER and SUPERCARD_DB_PASSWORD."
        }
        ensureSchema()

        var imported = 0
        var skipped = 0
        var failed = 0
        val errors = mutableListOf<String>()

        for (link in links) {
            try {
                val exists = receiptRepository.existsByExternalId(SOURCE, link.externalReceiptId)
                if (exists) {
                    skipped++
                    continue
                }

                val pdf = supercardHttpClient.downloadReceiptPdf(link.receiptUrl, cookieHeader)
                val tempFile = Files.createTempFile("supercard-", ".pdf").toFile()
                try {
                    tempFile.writeBytes(pdf)
                    val extraction = extractionService.analyzeReceipt(tempFile)
                    receiptRepository.insertReceiptWithArticles(extraction, SOURCE, link.externalReceiptId)
                    imported++
                } finally {
                    tempFile.delete()
                }
            } catch (e: Exception) {
                failed++
                val msg = "${link.externalReceiptId}: ${e.message}"
                errors.add(msg)
                LOGGER.log(Level.WARNING, "Supercard sync failed for ${link.externalReceiptId}", e)
            }
        }

        return SupercardSyncResponse(
            importedReceipts = imported,
            skippedReceipts = skipped,
            failedReceipts = failed,
            errors = errors
        )
    }

    private fun resolveCookieHeader(): String {
        require(properties.encryptionKey.isNotBlank()) { "SUPERCARD_SESSION_ENC_KEY is required" }
        val encrypted = if (!propertiesMissingForDb()) {
            ensureSchema()
            configStore.getEncrypted(KEY_COOKIE) ?: runtimeCookieEncrypted
        } else {
            runtimeCookieEncrypted
        }
        require(!encrypted.isNullOrBlank()) { "No Supercard session configured. Call POST /session first." }
        return cookieCrypto.decrypt(encrypted)
    }

    private fun parseAvailableLinks(cookieHeader: String): List<SupercardReceiptLink> {
        val html = supercardHttpClient.fetchReceiptOverviewHtml(cookieHeader)
        LOGGER.info("HTML Body Start: ${html.take(500)}")
        runCatching { htmlParser.parseReceiptLinks(html) }
            .onSuccess { return it }
            .onFailure {
                LOGGER.warning(
                    "[supercard] parse failed on overview. htmlChars=${html.length} " +
                        "containsReceiptAttr=${html.contains("data-receipturl", ignoreCase = true)} " +
                        "containsKassenzettelPoc=${html.contains("kassenzettelpoc", ignoreCase = true)}"
                )
            }

        val candidates = supercardHttpClient.fetchDigitalReceiptCandidates(cookieHeader)
        for ((idx, candidate) in candidates.withIndex()) {
            runCatching { htmlParser.parseReceiptLinks(candidate) }
                .onSuccess {
                    LOGGER.info("[supercard] parsed receipt links from fallback candidate index=$idx")
                    return it
                }
                .onFailure {
                    LOGGER.info(
                        "[supercard] fallback candidate index=$idx had no links " +
                            "(chars=${candidate.length})"
                    )
                }
        }

        throw IllegalStateException("SUPERCARD_PARSE_ERROR: No receipt links found in Supercard response")
    }

    private fun resolveSingleLink(request: SupercardSyncSingleRequest): SupercardReceiptLink {
        val explicitUrl = request.receiptUrl?.trim().orEmpty()
        if (explicitUrl.isNotBlank()) {
            return SupercardReceiptLink(
                receiptUrl = explicitUrl,
                externalReceiptId = htmlParser.extractExternalReceiptId(explicitUrl)
            )
        }

        val bc = request.bc?.trim().orEmpty()
        require(bc.isNotBlank()) { "receiptUrl or bc is required for sync-single" }
        val encodedBc = URLEncoder.encode(bc, StandardCharsets.UTF_8)
        val url = "https://www.supercard.ch/bin/coop/kbk/kassenzettelpoc?bc=$encodedBc&pdfType=receipt"
        return SupercardReceiptLink(
            receiptUrl = url,
            externalReceiptId = bc
        )
    }

    private fun ensureSchema() {
        configStore.ensureSchema()
        receiptRepository.ensureSchema()
    }

    private fun propertiesMissingForDb(): Boolean {
        return properties.dbUrl.isBlank() || properties.dbUser.isBlank() || properties.dbPassword.isBlank()
    }
}
