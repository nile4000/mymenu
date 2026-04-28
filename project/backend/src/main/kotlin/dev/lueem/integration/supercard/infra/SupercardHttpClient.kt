package dev.lueem.integration.supercard.infra

import jakarta.enterprise.context.ApplicationScoped
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.logging.Logger

@ApplicationScoped
class SupercardHttpClient {

    companion object {
        private val LOGGER = Logger.getLogger(SupercardHttpClient::class.java.name)
        private const val BROWSER_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36"
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
            .header("User-Agent", BROWSER_UA)
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Referer", "https://www.supercard.ch/de/app-digitale-services/meine-einkaeufe.html")
            .header("sec-fetch-dest", "empty")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-site", "same-origin")
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        LOGGER.info(
            "[supercard] purchases page=$page status=${response.statusCode()} " +
                "bodyChars=${response.body().length} bodyPreview='${preview(response.body())}'"
        )
        validateResponse(response.statusCode(), response.headers().firstValue("location").orElse(null))
        return response.body()
    }

    fun downloadReceiptPdf(url: String, cookieHeader: String): ByteArray {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Cookie", cookieHeader)
            .header("User-Agent", BROWSER_UA)
            .header("Referer", "https://www.supercard.ch/de/app-digitale-services/meine-einkaeufe.html")
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

}
