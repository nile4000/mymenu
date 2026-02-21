package dev.lueem.shared.client

import dev.lueem.shared.config.OpenAiProperties
import dev.lueem.shared.error.UpstreamOpenAiException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.json.bind.JsonbBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.logging.Level
import java.util.logging.Logger

/**
 * HTTP client for the OpenAI chat-completions API.
 *
 * Responsibilities:
 * - Build and execute HTTP requests
 * - Translate HTTP / network errors into UpstreamOpenAiException
 * - Return the raw response body string
 *
 * Does NOT contain business logic, prompt construction, or JSON parsing.
 */
@ApplicationScoped
class OpenAiClient @Inject constructor(
    private val properties: OpenAiProperties
) {
    companion object {
        private val LOGGER = Logger.getLogger(OpenAiClient::class.java.name)
        private const val OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions"
    }

    private val resolvedApiKey: String
        get() = properties.apiKey.takeIf { it.isNotBlank() }
            ?: throw UpstreamOpenAiException("OPENAI_API_KEY is not configured")

    private val httpClient = HttpClient.newHttpClient()
    private val jsonb = JsonbBuilder.create()

    /**
     * Executes a chat-completion request and returns the message content string.
     * Throws UpstreamOpenAiException on any failure.
     */
    fun chatCompletion(
        systemPrompt: String,
        userPrompt: String,
        model: String,
        temperature: Double
    ): String {
        val requestBody = OpenAiRequest(
            model = model,
            messages = listOf(
                OpenAiMessage("system", systemPrompt),
                OpenAiMessage("user", userPrompt)
            ),
            temperature = temperature
        )
        val responseBody = executeRequest(requestBody)
        val response = jsonb.fromJson(responseBody, OpenAiResponse::class.java)
        return response.choices?.firstOrNull()?.message?.content
            ?: throw UpstreamOpenAiException("OpenAI response missing message content")
    }

    private fun executeRequest(requestBody: OpenAiRequest): String {
        return try {
            val jsonPayload = jsonb.toJson(requestBody)
            val request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_CHAT_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $resolvedApiKey")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val status = response.statusCode()
            if (status < 200 || status >= 300) {
                throw UpstreamOpenAiException("OpenAI HTTP $status")
            }
            response.body()
        } catch (e: UpstreamOpenAiException) {
            throw e
        } catch (e: Exception) {
            LOGGER.log(Level.SEVERE, "OpenAI request failed", e)
            throw UpstreamOpenAiException("OpenAI request failed", e)
        }
    }
}
