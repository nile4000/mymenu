package dev.lueem.client

import dev.lueem.model.openai.OpenAiRequest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.json.Json
import jakarta.json.JsonArray
import jakarta.json.bind.JsonbBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.logging.Level
import java.util.logging.Logger
import org.eclipse.microprofile.config.inject.ConfigProperty

/**
 * Client for interacting with the OpenAI API to extract information from text.
 *
 * Responsibilities:
 * - Build the chat completion request
 * - Send the request
 * - Parse the structured JSON reply
 */
@ApplicationScoped
class OpenAiClient {

    @Inject
    constructor(
        requestFactory: OpenAiChatRequestFactory,
        responseParser: OpenAiArticleListParser
    ) {
        this.requestFactory = requestFactory
        this.responseParser = responseParser
    }

    private val requestFactory: OpenAiChatRequestFactory
    private val responseParser: OpenAiArticleListParser

    @ConfigProperty(name = "OPENAI_API_KEY")
    var openaiKey: String? = null

    private val httpClient = HttpClient.newHttpClient()
    private val jsonb = JsonbBuilder.create()

    companion object {
        private val LOGGER = Logger.getLogger(OpenAiClient::class.java.name)

        private const val OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions"
    }

    /**
     * Sends a question to the OpenAI API and retrieves the answer as a JSON array.
     *
     * @param question The question or prompt to send to OpenAI.
     * @return A JsonArray containing the extracted data, or an empty array if an error occurs.
     */
    fun extractArticlesFromText(question: String): JsonArray {
        return try {
            if (openaiKey.isNullOrBlank()) {
                throw IllegalArgumentException("API KEY is not set")
            }

            val requestBody = createRequestBody(question)
            val jsonPayload = jsonb.toJson(requestBody)

            val request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(OPENAI_CHAT_URL))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + openaiKey!!)
                            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                            .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val status = response.statusCode()
            if (status < 200 || status >= 300) {
                LOGGER.severe("OpenAI HTTP $status.")
                return Json.createArrayBuilder().build()
            }

            responseParser.parseArticleList(response.body(), jsonb)
        } catch (e: Exception) {
            LOGGER.log(Level.SEVERE, "Error in extractArticlesFromText", e)
            Json.createArrayBuilder().build()
        }
    }

    /**
     * Creates the request body for the OpenAI API call.
     *
     * @param question The user's question to be included in the request.
     * @return An OpenAiRequest object containing the model, messages, and temperature.
     */
    private fun createRequestBody(question: String): OpenAiRequest {
        return requestFactory.buildChatCompletionRequest(question)
    }
}
