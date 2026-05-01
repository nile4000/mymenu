package dev.lueem.integration.supercard.app

import dev.lueem.categorization.api.dto.Categorize
import dev.lueem.categorization.api.dto.CategorizeRequest
import dev.lueem.categorization.infra.CategorizationClient
import dev.lueem.categorization.infra.ProductNameCleaner
import dev.lueem.extraction.api.ReceiptResponse
import dev.lueem.extraction.app.ExtractionService
import dev.lueem.integration.supercard.api.dto.SupercardAvailableReceipt
import dev.lueem.integration.supercard.api.dto.SupercardAvailableResponse
import dev.lueem.integration.supercard.api.dto.SupercardSessionRequest
import dev.lueem.integration.supercard.api.dto.SupercardStatusResponse
import dev.lueem.integration.supercard.api.dto.SupercardSyncResponse
import dev.lueem.integration.supercard.domain.SupercardCooldownException
import dev.lueem.integration.supercard.domain.SupercardRemoteAccessException
import dev.lueem.integration.supercard.domain.SupercardReceiptLink
import dev.lueem.integration.supercard.domain.SupercardSyncInProgressException
import dev.lueem.integration.supercard.infra.CookieCrypto
import dev.lueem.integration.supercard.infra.SupercardConfigStore
import dev.lueem.integration.supercard.infra.SupercardHtmlParser
import dev.lueem.integration.supercard.infra.SupercardHttpClient
import dev.lueem.integration.supercard.infra.SupercardReceiptRepository
import dev.lueem.shared.config.SupercardProperties
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.nio.file.Files
import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock
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
    private val categorizationClient: CategorizationClient,
    private val productNameCleaner: ProductNameCleaner,
    private val properties: SupercardProperties
) {

    companion object {
        private val LOGGER = Logger.getLogger(SupercardService::class.java.name)
        private const val KEY_COOKIE = "supercard_session_encrypted"
        private const val SOURCE = "supercard"
        private val DEFAULT_COOLDOWN: Duration = Duration.ofMinutes(15)
    }

    @Volatile private var runtimeCookieEncrypted: String? = null
    @Volatile private var runtimeSessionUpdatedAt: Instant? = null
    @Volatile private var schemaEnsured: Boolean = false
    @Volatile private var nextAllowedSupercardRequestAt: Instant? = null
    private val syncLock = ReentrantLock()

    fun setSession(request: SupercardSessionRequest): SupercardStatusResponse {
        require(request.cookieHeader.isNotBlank()) { "cookieHeader must not be blank" }
        require(properties.encryptionKey.isNotBlank()) { "SUPERCARD_SESSION_ENC_KEY is required" }

        val encrypted = cookieCrypto.encrypt(request.cookieHeader.trim())
        runtimeCookieEncrypted = encrypted
        runtimeSessionUpdatedAt = Instant.now()
        nextAllowedSupercardRequestAt = null
        supercardHttpClient.resetRequestCounter()

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
        val links = getImportableLinks(cookieHeader)
        return SupercardAvailableResponse(
            count = links.size,
            receipts = links.map { it.toDto() }
        )
    }

    private fun getImportableLinks(cookieHeader: String): List<SupercardReceiptLink> {
        val links = parseAvailableLinks(cookieHeader)
        if (propertiesMissingForDb()) return links
        ensureSchema()
        val existingIds = receiptRepository.findExistingExternalIds(SOURCE, links.map { it.supercardReceiptBarcode })
        return links.filterNot { it.supercardReceiptBarcode in existingIds }
    }

    fun syncAvailable(): SupercardSyncResponse {
        if (!syncLock.tryLock()) {
            throw SupercardSyncInProgressException()
        }
        try {
            val cookieHeader = resolveCookieHeader()
            val links = getImportableLinks(cookieHeader)
            return syncLinks(cookieHeader, links)
        } finally {
            syncLock.unlock()
        }
    }

    private fun syncLinks(cookieHeader: String, links: List<SupercardReceiptLink>): SupercardSyncResponse {
        require(!propertiesMissingForDb()) {
            "Missing Supercard DB config. Set SUPERCARD_DB_URL, SUPERCARD_DB_USER and SUPERCARD_DB_PASSWORD."
        }
        ensureSchema()

        var imported = 0
        var failed = 0
        val errors = mutableListOf<String>()
        val linksToSync = links.take(properties.maxReceiptsPerSync)
        val deferred = links.size - linksToSync.size
        if (deferred > 0) {
            LOGGER.info("[supercard] deferring $deferred receipt(s) to next sync run")
        }

        for ((index, link) in linksToSync.withIndex()) {
            try {
                ensureRemoteRequestsAllowed()
                if (index > 0) {
                    pauseBetweenPdfDownloads()
                }
                val pdf = supercardHttpClient.downloadReceiptPdf(link.receiptUrl, cookieHeader)
                val tempFile = Files.createTempFile("supercard-", ".pdf").toFile()
                try {
                    tempFile.writeBytes(pdf)
                    val extraction = extractionService.analyzeReceipt(tempFile)
                    val categorized = assignCategories(extraction)
                    receiptRepository.insertReceiptWithArticles(categorized, SOURCE, link.supercardReceiptBarcode, link.purchaseDate, link.totalChf, link.locationName)
                    imported++
                } finally {
                    tempFile.delete()
                }
            } catch (e: SupercardRemoteAccessException) {
                failed++
                val retryAt = startCooldown(e)
                val msg = "${link.supercardReceiptBarcode}: ${e.message}; paused until $retryAt"
                errors.add(msg)
                LOGGER.log(Level.WARNING, "Supercard sync paused for ${link.supercardReceiptBarcode}", e)
                break
            } catch (e: SupercardCooldownException) {
                throw e
            } catch (e: Exception) {
                failed++
                val msg = "${link.supercardReceiptBarcode}: ${e.message}"
                errors.add(msg)
                LOGGER.log(Level.WARNING, "Supercard sync failed for ${link.supercardReceiptBarcode}", e)
            }
        }

        return SupercardSyncResponse(
            importedReceipts = imported,
            deferredReceipts = deferred,
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

    private fun ensureRemoteRequestsAllowed() {
        val retryAt = nextAllowedSupercardRequestAt ?: return
        val now = Instant.now()
        if (now.isBefore(retryAt)) {
            throw SupercardCooldownException(retryAt)
        }
        nextAllowedSupercardRequestAt = null
    }

    private fun startCooldown(e: SupercardRemoteAccessException): Instant {
        val duration = e.retryAfter
            ?.takeIf { !it.isNegative && !it.isZero }
            ?: DEFAULT_COOLDOWN
        val retryAt = Instant.now().plus(duration)
        if (nextAllowedSupercardRequestAt == null || retryAt.isAfter(nextAllowedSupercardRequestAt)) {
            nextAllowedSupercardRequestAt = retryAt
        }
        LOGGER.warning(
            "[supercard] remote access paused until $nextAllowedSupercardRequestAt " +
                "(status=${e.uploadStatus}, retryAfter=${e.retryAfter})"
        )
        return nextAllowedSupercardRequestAt!!
    }

    private fun pauseBetweenPdfDownloads() {
        try {
            Thread.sleep(Duration.ofSeconds(properties.pdfDownloadDelaySeconds).toMillis())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Supercard sync was interrupted", e)
        }
    }

    private fun parseAvailableLinks(cookieHeader: String): List<SupercardReceiptLink> {
        // Supercard returns max 20 per page — fetch all pages until we get a partial page
        val allLinks = mutableListOf<SupercardReceiptLink>()
        val seenReceiptBarcodes = mutableSetOf<String>()
        var page = 0
        var pagesFetched = 0
        while (page < properties.maxPurchasePages) {
            val json = supercardHttpClient.fetchPurchasesJson(cookieHeader, page)
            pagesFetched++
            val pageLinks = htmlParser.parsePurchasesJson(json)
            val newPageLinks = pageLinks.filter { seenReceiptBarcodes.add(it.supercardReceiptBarcode) }
            if (pageLinks.isNotEmpty() && newPageLinks.isEmpty()) {
                LOGGER.info("[supercard] stopping purchases paging at page=$page because it repeated a previous page")
                break
            }
            allLinks.addAll(newPageLinks)
            if (pageLinks.size != properties.pageSize) break
            page++
        }
        val hitPageLimit = pagesFetched >= properties.maxPurchasePages
        LOGGER.info(
            "[supercard] found ${allLinks.size} available receipts across $pagesFetched page(s)" +
                if (hitPageLimit) " (capped at ${properties.maxPurchasePages} pages)" else ""
        )
        return allLinks
    }

    private fun SupercardReceiptLink.toDto() = SupercardAvailableReceipt(
        receiptUrl = receiptUrl,
        supercardReceiptBarcode = supercardReceiptBarcode,
        locationName = locationName,
        logoUrl = logoUrl,
        purchaseDate = purchaseDate,
        totalChf = totalChf?.toPlainString(),
    )

    private fun ensureSchema() {
        if (schemaEnsured) return
        configStore.ensureSchema()
        receiptRepository.ensureSchema()
        schemaEnsured = true
    }

    private fun assignCategories(extraction: ReceiptResponse): ReceiptResponse {
        if (extraction.articles.isEmpty()) return extraction
        val request = CategorizeRequest(
            items = extraction.articles.mapIndexed { index, article ->
                Categorize(id = index.toString(), name = productNameCleaner.clean(article.name))
            }
        )
        return try {
            val results = categorizationClient.categorize(request).associateBy { it.id }
            val enriched = extraction.articles.mapIndexed { index, article ->
                val category = results[index.toString()]?.category ?: article.category
                if (category != article.category) article.copy(category = category) else article
            }
            extraction.copy(articles = enriched)
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "[supercard] categorization failed, articles saved without category", e)
            extraction
        }
    }

    private fun propertiesMissingForDb(): Boolean {
        return properties.dbUrl.isBlank() || properties.dbUser.isBlank() || properties.dbPassword.isBlank()
    }
}
