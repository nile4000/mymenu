package dev.lueem.client

import dev.lueem.model.openai.OpenAiResponse
import jakarta.enterprise.context.ApplicationScoped
import jakarta.json.Json
import jakarta.json.JsonArray
import jakarta.json.bind.Jsonb
import java.io.StringReader
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Parses the OpenAI response and extracts the article list JSON.
 */
@ApplicationScoped
class OpenAiArticleListParser {

    companion object {
        private val LOGGER = Logger.getLogger(OpenAiArticleListParser::class.java.name)
    }

    fun parseArticleList(responseBody: String, jsonb: Jsonb): JsonArray {
        return runCatching {
            val openAiResponse = jsonb.fromJson(responseBody, OpenAiResponse::class.java)
            if (openAiResponse.choices.isNullOrEmpty()) {
                LOGGER.severe("OpenAI response missing choices.")
            }
            val contentString = extractContent(openAiResponse)
            val sanitizedContent = OpenAiJsonSanitizer.sanitize(contentString)
            val parsed = extractArticleList(sanitizedContent)
            parsed
        }.getOrElse { e ->
            LOGGER.log(Level.SEVERE, "Error processing response body", e)
            throw RuntimeException("Error processing response", e)
        }
    }

    private fun extractContent(response: OpenAiResponse): String {
        val choice = response.choices?.firstOrNull()
        requireNotNull(choice) { "No choices found in the response." }
        return requireNotNull(choice.message?.content) {
            "No message content found in the first choice."
        }
    }

    private fun extractArticleList(jsonText: String): JsonArray {
        Json.createReader(StringReader(jsonText)).use { reader ->
            val jsonObject = reader.readObject()
            return jsonObject.getJsonArray("ArticleList")
                    ?: Json.createArrayBuilder().build()
        }
    }

}
