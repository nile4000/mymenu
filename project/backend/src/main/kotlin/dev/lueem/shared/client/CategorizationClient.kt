package dev.lueem.shared.client

import dev.lueem.ai.api.dto.CategorizeRequest
import dev.lueem.ai.api.dto.CategorizeResultItem
import dev.lueem.shared.config.CategorizationProperties
import dev.lueem.shared.error.UploadCategorizationException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.json.bind.JsonbBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@ApplicationScoped
class CategorizationClient @Inject constructor(
    private val properties: CategorizationProperties
) {
    companion object {
        private const val CATEGORIZE_PATH = "/categorize"
    }

    private val httpClient = HttpClient.newHttpClient()
    private val jsonb = JsonbBuilder.create()

    fun categorize(request: CategorizeRequest): List<CategorizeResultItem> {
        val payload = jsonb.toJson(request)
        return try {
            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("${properties.serviceUrl}$CATEGORIZE_PATH"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build()
            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw UploadCategorizationException("Categorization service HTTP ${response.statusCode()}")
            }
            jsonb.fromJson(response.body(), Array<CategorizeResultItem>::class.java).toList()
        } catch (e: UploadCategorizationException) {
            throw e
        } catch (e: Exception) {
            throw UploadCategorizationException("Categorization service request failed", e)
        }
    }
}
