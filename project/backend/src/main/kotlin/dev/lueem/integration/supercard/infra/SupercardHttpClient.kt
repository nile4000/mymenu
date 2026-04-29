package dev.lueem.integration.supercard.infra

import jakarta.enterprise.context.ApplicationScoped
import dev.lueem.integration.supercard.domain.SupercardRemoteAccessException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

@ApplicationScoped
class SupercardHttpClient {

    companion object {
        private val LOGGER = Logger.getLogger(SupercardHttpClient::class.java.name)
        private const val APP_USER_AGENT = "MyMenu/0.1 receipt-sync"
    }

    private val requestCount = AtomicInteger(0)
    private var sessionStartedAt: Instant = Instant.now()

    fun resetRequestCounter() {
        requestCount.set(0)
        sessionStartedAt = Instant.now()
        LOGGER.info("[supercard] request counter reset")
    }

    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build()

    fun fetchPurchasesJson(cookieHeader: String, page: Int = 0): String {
        // totalAmountMin/Max use "undefined00" intentionally — this mirrors what the Supercard
        // browser app sends (a JavaScript undefined coerced to string + "00"). Omitting these
        // params or using valid values causes the server to return an empty result.
        val url = "https://www.supercard.ch/bin/coop/supercard/digitalReceipt/getPurchases.json" +
            "?currentPage=$page&totalAmountMin=undefined00&totalAmountMax=undefined00"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Cookie", cookieHeader)
            .header("User-Agent", APP_USER_AGENT)
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Referer", "https://www.supercard.ch/de/app-digitale-services/meine-einkaeufe.html")
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val n = requestCount.incrementAndGet()
        val elapsed = Duration.between(sessionStartedAt, Instant.now()).toSeconds()
        LOGGER.info("[supercard] req#$n (+${elapsed}s) purchases page=$page status=${response.statusCode()} chars=${response.body().length}")
        validateResponse(response.statusCode(), response.headers())
        return response.body()
    }

    fun downloadReceiptPdf(url: String, cookieHeader: String): ByteArray {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Cookie", cookieHeader)
            .header("User-Agent", APP_USER_AGENT)
            .header("Referer", "https://www.supercard.ch/de/app-digitale-services/meine-einkaeufe.html")
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        val n = requestCount.incrementAndGet()
        val elapsed = Duration.between(sessionStartedAt, Instant.now()).toSeconds()
        LOGGER.fine("[supercard] req#$n (+${elapsed}s) pdf status=${response.statusCode()} bytes=${response.body().size}")
        validateResponse(response.statusCode(), response.headers())
        return response.body()
    }

    private fun validateResponse(statusCode: Int, headers: HttpHeaders) {
        val location = headers.firstValue("location").orElse(null)
        val retryAfter = parseRetryAfter(headers.firstValue("retry-after").orElse(null))
        if (statusCode == 401 || statusCode == 403) {
            throw SupercardRemoteAccessException("Supercard session is invalid or expired", statusCode, retryAfter)
        }
        if (statusCode == 429) {
            throw SupercardRemoteAccessException("Supercard rate limit reached", statusCode, retryAfter)
        }
        if (statusCode in 300..399 && (location?.contains("login", ignoreCase = true) == true)) {
            throw SupercardRemoteAccessException("Supercard session is invalid or expired", 401, retryAfter)
        }
        if (statusCode !in 200..299) {
            throw IllegalStateException("Supercard request failed with status $statusCode")
        }
    }

    private fun parseRetryAfter(value: String?): Duration? {
        val trimmed = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        trimmed.toLongOrNull()?.let { seconds ->
            return Duration.ofSeconds(seconds.coerceAtLeast(0))
        }
        return runCatching {
            val retryAt = ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
            Duration.between(Instant.now(), retryAt).takeIf { !it.isNegative }
        }.getOrNull()
    }

    private fun preview(body: String, maxLen: Int = 500): String =
        body
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .take(maxLen)

}
