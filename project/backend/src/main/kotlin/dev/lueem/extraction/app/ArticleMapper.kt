package dev.lueem.extraction.app

import dev.lueem.extraction.api.ArticleDto
import dev.lueem.extraction.domain.Article
import jakarta.enterprise.context.ApplicationScoped
import jakarta.json.Json
import jakarta.json.JsonArray
import jakarta.json.JsonObjectBuilder
import jakarta.json.JsonValue
import java.math.BigDecimal
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Maps OpenAI JSON output → domain Article, and domain Article → ArticleDto.
 *
 * Two responsibilities:
 *  1. sanitizeArticleJsonArray  — normalise raw AI JSON (fill missing fields)
 *  2. mapToArticles             — convert sanitised JSON to immutable domain objects
 *  3. Article.toDto()           — extension fun for api-layer mapping
 */
@ApplicationScoped
class ArticleMapper {

    companion object {
        private val LOGGER = Logger.getLogger(ArticleMapper::class.java.name)
    }

    fun sanitizeArticleJsonArray(articlesJson: JsonArray, traceId: String? = null): JsonArray {
        val builder = Json.createArrayBuilder()
        var nonObjectCount = 0
        var defaultedNameCount = 0
        var blankInputNameCount = 0
        for (jsonValue in articlesJson) {
            val jsonObject = jsonValue.asObjectOrNull()
            if (jsonObject == null) {
                nonObjectCount++
                continue
            }
            if (jsonObject.containsKey(ArticleJsonSchema.NAME_FIELD) && !jsonObject.isNull(ArticleJsonSchema.NAME_FIELD)) {
                runCatching { jsonObject.getString(ArticleJsonSchema.NAME_FIELD) }
                    .onSuccess { if (it.isBlank()) blankInputNameCount++ }
            }
            val objectBuilder = Json.createObjectBuilder()
            for (spec in ArticleJsonSchema.FIELD_SPECS) {
                val usedDefault = addFieldOrDefault(objectBuilder, jsonObject, spec)
                if (spec.name == ArticleJsonSchema.NAME_FIELD && usedDefault) {
                    defaultedNameCount++
                }
            }
            builder.add(objectBuilder)
        }
        val ctx = traceId?.let { "[extract:$it] " } ?: ""
        LOGGER.info(
            "${ctx}sanitizeArticleJsonArray input=${articlesJson.size} nonObject=$nonObjectCount defaultedName=$defaultedNameCount blankInputName=$blankInputNameCount"
        )
        return builder.build()
    }

    fun mapToArticles(articlesJson: JsonArray, traceId: String? = null): List<Article> {
        val mapped = mutableListOf<Article>()
        var blankNameCount = 0
        var unknownNameCount = 0

        articlesJson
            .mapNotNull { it.asArticleObjectOrNull() }
            .forEach { jsonObject ->
                try {
                    val name = readString(jsonObject, ArticleJsonSchema.NAME_FIELD, "Unknown")
                    val price = readBigDecimal(jsonObject, ArticleJsonSchema.PRICE_FIELD)
                    val rawQty = readBigDecimal(jsonObject, ArticleJsonSchema.QUANTITY_FIELD)
                    val quantity = if (rawQty < BigDecimal.ZERO) BigDecimal.ZERO else rawQty
                    val discount = readBigDecimal(jsonObject, ArticleJsonSchema.DISCOUNT_FIELD)
                    val calculatedTotal = price.multiply(quantity).subtract(discount)
                        .let { if (it < BigDecimal.ZERO) BigDecimal.ZERO else it }

                    if (name.isBlank()) blankNameCount++
                    if (name == "Unknown") unknownNameCount++

                    mapped += Article(
                        name = name,
                        price = price,
                        quantity = quantity,
                        discount = discount,
                        total = calculatedTotal
                        // category filled later by the frontend; defaults to ""
                    )
                } catch (e: NumberFormatException) {
                    LOGGER.log(Level.SEVERE, "Error parsing article fields: $jsonObject", e)
                } catch (e: ClassCastException) {
                    LOGGER.log(Level.SEVERE, "Error parsing article fields: $jsonObject", e)
                }
            }

        val ctx = traceId?.let { "[extract:$it] " } ?: ""
        LOGGER.info(
            "${ctx}mapToArticles mapped=${mapped.size} blankName=$blankNameCount unknownName=$unknownNameCount"
        )

        return mapped
    }

    // --- private helpers ---

    private fun JsonValue.asObjectOrNull(): jakarta.json.JsonObject? =
        if (valueType == JsonValue.ValueType.OBJECT) asJsonObject()
        else { LOGGER.warning("Non-object JSON value in articles array: $this"); null }

    private fun JsonValue.asArticleObjectOrNull(): jakarta.json.JsonObject? =
        if (valueType == JsonValue.ValueType.OBJECT) asJsonObject()
        else { LOGGER.warning("Invalid JSON value type for article, found: $valueType"); null }

    private fun addFieldOrDefault(
        builder: JsonObjectBuilder,
        jsonObject: jakarta.json.JsonObject,
        spec: ArticleFieldSpec
    ): Boolean {
        if (jsonObject.containsKey(spec.name) && !jsonObject.isNull(spec.name)) {
            try {
                builder.add(spec.name, jsonObject[spec.name])
                return false
            } catch (e: Exception) {
                LOGGER.log(Level.WARNING, "Invalid value for field '${spec.name}': $jsonObject", e)
            }
        }
        when (val d = spec.defaultValue) {
            is String -> builder.add(spec.name, d)
            is Double -> builder.add(spec.name, d)
            else -> builder.addNull(spec.name)
        }
        return true
    }

    private fun readString(obj: jakarta.json.JsonObject, field: String, default: String): String =
        if (obj.containsKey(field) && !obj.isNull(field)) obj.getString(field) else default

    private fun readBigDecimal(obj: jakarta.json.JsonObject, field: String): BigDecimal =
        if (obj.containsKey(field) && !obj.isNull(field))
            obj.getJsonNumber(field).bigDecimalValue()
        else BigDecimal.ZERO
}

/** Converts a domain Article to the API response DTO. */
fun Article.toDto(): ArticleDto = ArticleDto(
    name = name,
    price = price,
    quantity = quantity,
    discount = discount,
    total = total,
    category = category
)
