package dev.lueem.categorization.infra

import dev.lueem.categorization.api.dto.CategorizeRequest
import dev.lueem.categorization.api.dto.CategorizeResult
import dev.lueem.shared.config.CategorizationProperties
import dev.lueem.shared.error.UploadCategorizationException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.json.bind.JsonbBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.logging.Logger

@ApplicationScoped
class CategorizationClient @Inject constructor(
    private val properties: CategorizationProperties
) {
    companion object {
        private val LOGGER = Logger.getLogger(CategorizationClient::class.java.name)
        private const val CATEGORIZE_PATH = "/categorize"
        private const val CONTENT_TYPE = "Content-Type"
        private const val APPLICATION_JSON = "application/json"
    }

    private val httpClient = HttpClient.newHttpClient()
    private val jsonb = JsonbBuilder.create()

    fun categorize(request: CategorizeRequest): List<CategorizeResult> {
        require(request.items.isNotEmpty()) { "Die Liste darf nicht leer sein" }
        require(request.items.size <= 40) { "Die Liste darf maximal 40 Einträge enthalten" }
        require(request.items.all { it.id.isNotBlank() && it.name.isNotBlank() }) {
            "Jeder Eintrag benötigt eine ID und einen Namen"
        }
        return try {
            val response = httpClient.send(
                buildCategorizeRequest(request),
                HttpResponse.BodyHandlers.ofString()
            )

            validateResponse(response)
            jsonb.fromJson(response.body(), Array<CategorizeResult>::class.java).toList()
        } catch (e: UploadCategorizationException) {
            throw e
        } catch (e: Exception) {
            throw UploadCategorizationException("Categorization service request failed", e)
        }
    }

    private fun buildCategorizeRequest(request: CategorizeRequest): HttpRequest {
        val payload = jsonb.toJson(request)

        return HttpRequest.newBuilder()
            .uri(URI.create("${properties.serviceUrl}$CATEGORIZE_PATH"))
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()
    }

    private fun validateResponse(response: HttpResponse<String>) {
        if (response.statusCode() !in 200..299) {
            throw UploadCategorizationException(
                "Categorization service HTTP ${response.statusCode()}"
            )
        }
    }
}
