package dev.lueem.extraction.infra

import dev.lueem.shared.util.JsonSanitizer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.json.Json
import jakarta.json.JsonArray
import java.io.StringReader
import java.util.logging.Level
import java.util.logging.Logger

/** Parses a JSON string containing an "ArticleList" key into a JsonArray. */
@ApplicationScoped
class ArticleListParser {

    companion object {
        private val LOGGER = Logger.getLogger(ArticleListParser::class.java.name)
        private const val KEY_ARTICLE_LIST = "ArticleList"
    }

    /**
     * Parses the raw content string (e.g. from an AI or external API).
     * Returns an empty array if parsing fails.
     */
    fun parse(content: String): JsonArray {
        return runCatching {
            val sanitized = JsonSanitizer.sanitize(content, '{', "\"$KEY_ARTICLE_LIST\"")
            LOGGER.info("parse sanitizedChars=${sanitized.length}")
            LOGGER.fine("parse sanitizedPreview='${preview(sanitized)}'")
            extractArticleList(sanitized)
        }.getOrElse { e ->
            LOGGER.log(Level.SEVERE, "Error parsing article list from content: $content", e)
            Json.createArrayBuilder().build()
        }
    }

    private fun extractArticleList(jsonText: String): JsonArray {
        Json.createReader(StringReader(jsonText)).use { reader ->
            val obj = reader.readObject()
            if (!obj.containsKey(KEY_ARTICLE_LIST)) {
                LOGGER.warning("JSON missing '$KEY_ARTICLE_LIST'. Available keys=${obj.keys}")
            }
            val list = obj.getJsonArray(KEY_ARTICLE_LIST) ?: Json.createArrayBuilder().build()
            LOGGER.info("extracted items=${list.size}")
            return list
        }
    }

    private fun preview(text: String, maxLen: Int = 320): String =
        text.replace("\n", "\\n").replace("\r", "\\r").take(maxLen)
}