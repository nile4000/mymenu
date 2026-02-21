package dev.lueem.extraction.infra

import dev.lueem.shared.util.JsonSanitizer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.json.Json
import jakarta.json.JsonArray
import java.io.StringReader
import java.util.logging.Level
import java.util.logging.Logger

/** Parses the content string from an OpenAI response into an article-list JSON array. */
@ApplicationScoped
class OpenAiArticleListParser {

    companion object {
        private val LOGGER = Logger.getLogger(OpenAiArticleListParser::class.java.name)
    }

    /**
     * Parses the raw content string (representing the JSON output from AI).
     * Returns an empty array if parsing fails.
     */
    fun parseArticleListFromContent(content: String): JsonArray {
        return runCatching {
            val sanitized = JsonSanitizer.sanitize(content, '{', "\"ArticleList\"")
            LOGGER.info("parseArticleListFromContent sanitizedChars=${sanitized.length}")
            LOGGER.fine("parseArticleListFromContent sanitizedPreview='${preview(sanitized)}'")
            extractArticleList(sanitized)
        }.getOrElse { e ->
            LOGGER.log(Level.SEVERE, "Error parsing article list from AI content: $content", e)
            Json.createArrayBuilder().build()
        }
    }

    private fun extractArticleList(jsonText: String): JsonArray {
        Json.createReader(StringReader(jsonText)).use { reader ->
            val obj = reader.readObject()
            if (!obj.containsKey("ArticleList")) {
                LOGGER.warning("OpenAI JSON missing 'ArticleList'. Keys=${obj.keys}")
            }
            val list = obj.getJsonArray("ArticleList") ?: Json.createArrayBuilder().build()
            LOGGER.info("extractArticleList items=${list.size}")
            return list
        }
    }

    private fun preview(text: String, maxLen: Int = 320): String =
        text.replace("\n", "\\n").replace("\r", "\\r").take(maxLen)
}
