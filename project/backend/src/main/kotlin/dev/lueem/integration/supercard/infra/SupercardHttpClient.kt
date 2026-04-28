package dev.lueem.integration.supercard.infra

import jakarta.enterprise.context.ApplicationScoped
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.logging.Logger

@ApplicationScoped
class SupercardHttpClient {

    companion object {
        private val LOGGER = Logger.getLogger(SupercardHttpClient::class.java.name)
    }

    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build()

    fun fetchReceiptOverviewHtml(cookieHeader: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.supercard.ch/de/app-digitale-services/meine-einkaeufe.html"))
            .timeout(Duration.ofSeconds(20))
            .header("Cookie", cookieHeader)
            .header("User-Agent", "Mozilla/5.0")
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        LOGGER.info(
            "[supercard] overview status=${response.statusCode()} " +
                "location='${response.headers().firstValue("location").orElse("")}' " +
                "contentType='${response.headers().firstValue("content-type").orElse("")}' " +
                "bodyChars=${response.body().length}"
        )
        LOGGER.info("[supercard] overview bodyPreview='${preview(response.body())}'")
        validateResponse(response.statusCode(), response.headers().firstValue("location").orElse(null))
        return response.body()
    }

    fun downloadReceiptPdf(url: String, cookieHeader: String): ByteArray {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Cookie", cookieHeader)
            .header("User-Agent", "Mozilla/5.0")
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        LOGGER.info(
            "[supercard] pdf status=${response.statusCode()} " +
                "location='${response.headers().firstValue("location").orElse("")}' " +
                "contentType='${response.headers().firstValue("content-type").orElse("")}' " +
                "bytes=${response.body().size}"
        )
        validateResponse(response.statusCode(), response.headers().firstValue("location").orElse(null))
        return response.body()
    }

    fun fetchDigitalReceiptCandidates(cookieHeader: String): List<String> {
        val candidates = mutableListOf<String>()
        val urls = listOf(
            "https://www.supercard.ch/bin/coop/supercard/digitalReceipt/",
            "https://www.supercard.ch/bin/coop/supercard/digitalReceipt",
            "https://www.supercard.ch/bin/coop/supercard/digitalReceipt/?type=receipt",
            "https://www.supercard.ch/bin/coop/supercard/digitalReceipt?type=receipt"
        )

        for (url in urls) {
            runCatching { requestText(url, cookieHeader, method = "GET") }
                .onSuccess {
                    LOGGER.info("[supercard] candidate GET '$url' bodyChars=${it.length}")
                    candidates.add(it)
                }
                .onFailure { LOGGER.info("[supercard] candidate GET '$url' failed: ${it.message}") }
        }

        val postBodies = listOf(
            mapOf("type" to "receipt"),
            mapOf("page" to "1", "type" to "receipt"),
            mapOf("offset" to "0", "limit" to "500", "type" to "receipt"),
            mapOf("start" to "2021-01-01", "end" to "2030-12-31", "plattform" to "all", "type" to "receipt")
        )
        val postUrls = listOf(
            "https://www.supercard.ch/bin/coop/supercard/digitalReceipt/",
            "https://www.supercard.ch/bin/coop/supercard/digitalReceipt"
        )

        for (url in postUrls) {
            for (params in postBodies) {
                runCatching { requestText(url, cookieHeader, method = "POST", form = params) }
                    .onSuccess {
                        LOGGER.info("[supercard] candidate POST '$url' params=${params.keys} bodyChars=${it.length}")
                        candidates.add(it)
                    }
                    .onFailure {
                        LOGGER.info("[supercard] candidate POST '$url' params=${params.keys} failed: ${it.message}")
                    }
            }
        }

        return candidates
    }

    private fun validateResponse(statusCode: Int, location: String?) {
        if (statusCode == 401 || statusCode == 403) {
            throw IllegalStateException("Supercard session is invalid or expired")
        }
        if (statusCode in 300..399 && (location?.contains("login", ignoreCase = true) == true)) {
            throw IllegalStateException("Supercard session is invalid or expired")
        }
        if (statusCode !in 200..299) {
            throw IllegalStateException("Supercard request failed with status $statusCode")
        }
    }

    private fun preview(body: String, maxLen: Int = 500): String =
        body
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .take(maxLen)

    private fun requestText(
        url: String,
        cookieHeader: String,
        method: String,
        form: Map<String, String> = emptyMap()
    ): String {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Cookie", cookieHeader)
            .header("User-Agent", "Mozilla/5.0")
            .header("Accept", "text/html,application/json,application/xhtml+xml,*/*")
            .header("X-Requested-With", "XMLHttpRequest")

        val request = if (method == "POST") {
            val payload = form.entries.joinToString("&") {
                "${URLEncoder.encode(it.key, StandardCharsets.UTF_8)}=${URLEncoder.encode(it.value, StandardCharsets.UTF_8)}"
            }
            builder
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build()
        } else {
            builder.GET().build()
        }

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        validateResponse(response.statusCode(), response.headers().firstValue("location").orElse(null))
        return response.body()
    }
}
