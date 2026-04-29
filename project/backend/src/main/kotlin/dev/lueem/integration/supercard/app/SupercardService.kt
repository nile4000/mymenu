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
        private const val SOURCE = "supercard"
        private const val MAX_PURCHASE_PAGES = 50
        private const val SUPERCARD_PAGE_SIZE = 20
    }

    @Volatile private var runtimeCookieEncrypted: String? = null
    @Volatile private var runtimeSessionUpdatedAt: Instant? = null
    @Volatile private var schemaEnsured: Boolean = false

    fun setSession(request: SupercardSessionRequest): SupercardStatusResponse {
        require(request.cookieHeader.isNotBlank()) { "cookieHeader must not be blank" }
        require(properties.encryptionKey.isNotBlank()) { "SUPERCARD_SESSION_ENC_KEY is required" }

        val encrypted = cookieCrypto.encrypt(request.cookieHeader.trim())
        runtimeCookieEncrypted = encrypted
        runtimeSessionUpdatedAt = Instant.now()

        if (!propertiesMissingForDb()) {
            ensureSchema()
            configStore.upsertEncrypted(KEY_COOKIE, encrypted)
        }
        return SupercardStatusResponse(
            connected = true,
            sessionUpdatedAt = runtimeSessionUpdatedAt?.toString()
        )
    }

    fun getStatus(): SupercardStatusResponse {
        if (propertiesMissingForDb()) {
            return SupercardStatusResponse(
                connected = !runtimeCookieEncrypted.isNullOrBlank(),
                sessionUpdatedAt = runtimeSessionUpdatedAt?.toString()
            )
        }
        ensureSchema()
        val encrypted = configStore.getEncrypted(KEY_COOKIE) ?: runtimeCookieEncrypted
        val updatedAt = configStore.getUpdatedAt(KEY_COOKIE)?.toString() ?: runtimeSessionUpdatedAt?.toString()

        return SupercardStatusResponse(
            connected = !encrypted.isNullOrBlank(),
            sessionUpdatedAt = updatedAt
        )
    }

    fun available(): SupercardAvailableResponse {
        val cookieHeader = resolveCookieHeader()
        val links = parseAvailableLinks(cookieHeader)
        val importableLinks = if (propertiesMissingForDb()) {
            links
        } else {
            ensureSchema()
            val existingIds = receiptRepository.findExistingExternalIds(
                SOURCE,
                links.map { it.externalReceiptId }
            )
            links.filterNot { it.externalReceiptId in existingIds }
        }
        return SupercardAvailableResponse(
            count = importableLinks.size,
            receipts = importableLinks.map {
                SupercardAvailableReceipt(
                    receiptUrl = it.receiptUrl,
                    externalReceiptId = it.externalReceiptId,
                    locationName = it.locationName,
                    logoUrl = it.logoUrl,
                    purchaseDate = it.purchaseDate,
                    totalChf = it.totalChf?.toPlainString()
                )
            }
        )
    }

    // Imports a single receipt by direct URL or bc code — useful for manual re-sync or testing
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
        require(!propertiesMissingForDb()) {
            "Missing Supercard DB config. Set SUPERCARD_DB_URL, SUPERCARD_DB_USER and SUPERCARD_DB_PASSWORD."
        }
        ensureSchema()

        var imported = 0
        var skipped = 0
        var failed = 0
        val errors = mutableListOf<String>()
        val existingIds = receiptRepository.findExistingExternalIds(
            SOURCE,
            links.map { it.externalReceiptId }
        ).toMutableSet()

        for (link in links) {
            try {
                if (link.externalReceiptId in existingIds) {
                    skipped++
                    continue
                }

                Thread.sleep(500)
                val pdf = supercardHttpClient.downloadReceiptPdf(link.receiptUrl, cookieHeader)
                val tempFile = Files.createTempFile("supercard-", ".pdf").toFile()
                try {
                    tempFile.writeBytes(pdf)
                    val extraction = extractionService.analyzeReceipt(tempFile)
                    receiptRepository.insertReceiptWithArticles(extraction, SOURCE, link.externalReceiptId, link.purchaseDate, link.totalChf)
                    existingIds.add(link.externalReceiptId)
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
        // Supercard returns max 20 per page — fetch all pages until we get a partial page
        val allLinks = mutableListOf<SupercardReceiptLink>()
        var page = 0
        var pagesFetched = 0
        while (page < MAX_PURCHASE_PAGES) {
            val json = supercardHttpClient.fetchPurchasesJson(cookieHeader, page)
            pagesFetched++
            val pageLinks = htmlParser.parsePurchasesJson(json)
            allLinks.addAll(pageLinks)
            if (pageLinks.size < SUPERCARD_PAGE_SIZE) break
            page++
        }
        val hitPageLimit = pagesFetched >= MAX_PURCHASE_PAGES
        LOGGER.info(
            "[supercard] found ${allLinks.size} available receipts across $pagesFetched page(s)" +
                if (hitPageLimit) " (capped at $MAX_PURCHASE_PAGES pages)" else ""
        )
        return allLinks
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
        if (schemaEnsured) return
        configStore.ensureSchema()
        receiptRepository.ensureSchema()
        schemaEnsured = true
    }

    private fun propertiesMissingForDb(): Boolean {
        return properties.dbUrl.isBlank() || properties.dbUser.isBlank() || properties.dbPassword.isBlank()
    }
}
