package dev.lueem.service

import dev.lueem.model.Article
import jakarta.enterprise.context.ApplicationScoped
import jakarta.json.Json
import jakarta.json.JsonArray
import jakarta.json.JsonObjectBuilder
import jakarta.json.JsonValue
import java.math.BigDecimal
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Maps OpenAI JSON output into internal Article models and normalizes data.
 */
@ApplicationScoped
class ArticleJsonMapper {

    companion object {
        private val LOGGER = Logger.getLogger(ArticleJsonMapper::class.java.name)
    }

    fun sanitizeArticleJsonArray(articlesJson: JsonArray): JsonArray {
        val sanitizedArrayBuilder = Json.createArrayBuilder()

        for (jsonValue in articlesJson) {
            val jsonObject = jsonValue.asObjectOrNull() ?: continue
            val sanitizedObjectBuilder = Json.createObjectBuilder()

            for (spec in ArticleJsonSchema.FIELD_SPECS) {
                addFieldOrDefault(sanitizedObjectBuilder, jsonObject, spec)
            }

            sanitizedArrayBuilder.add(sanitizedObjectBuilder)
        }

        return sanitizedArrayBuilder.build()
    }

    fun mapToArticles(articlesJson: JsonArray): List<Article> {
        return articlesJson
            .mapNotNull { it.asArticleObjectOrNull() }
            .mapNotNull { jsonObject ->
                try {
                    Article().apply {
                        name = readString(jsonObject, ArticleJsonSchema.NAME_FIELD, "Unknown")
                        price = readBigDecimal(jsonObject, ArticleJsonSchema.PRICE_FIELD)
                        quantity = readBigDecimal(jsonObject, ArticleJsonSchema.QUANTITY_FIELD)
                        discount = readBigDecimal(jsonObject, ArticleJsonSchema.DISCOUNT_FIELD)
                        total = readBigDecimal(jsonObject, ArticleJsonSchema.TOTAL_FIELD)
                        // Category is filled later by the frontend.
                        category = ""
                    }.also(::normalizeArticleData)
                } catch (e: NumberFormatException) {
                    LOGGER.log(Level.SEVERE, "Error parsing article fields: $jsonObject", e)
                    null
                } catch (e: ClassCastException) {
                    LOGGER.log(Level.SEVERE, "Error parsing article fields: $jsonObject", e)
                    null
                }
            }
    }

    private fun JsonValue.asObjectOrNull(): jakarta.json.JsonObject? {
        return if (valueType == JsonValue.ValueType.OBJECT) {
            asJsonObject()
        } else {
            LOGGER.warning("Non-object JSON value found in articles array: $this")
            null
        }
    }

    private fun JsonValue.asArticleObjectOrNull(): jakarta.json.JsonObject? {
        return if (valueType == JsonValue.ValueType.OBJECT) {
            asJsonObject()
        } else {
            LOGGER.log(
                    Level.WARNING,
                    "Invalid JSON value type for article. Expected OBJECT, found: " + valueType
            )
            null
        }
    }

    private fun addFieldOrDefault(
        builder: JsonObjectBuilder,
        jsonObject: jakarta.json.JsonObject,
        spec: ArticleFieldSpec
    ) {
        if (jsonObject.containsKey(spec.name) && !jsonObject.isNull(spec.name)) {
            try {
                builder.add(spec.name, jsonObject[spec.name])
                return
            } catch (e: Exception) {
                LOGGER.log(
                        Level.WARNING,
                        "Invalid value for field '${spec.name}' in article: $jsonObject",
                        e
                )
            }
        }
        addDefaultField(builder, spec.name, spec.defaultValue)
    }

    private fun readString(
        jsonObject: jakarta.json.JsonObject,
        field: String,
        defaultValue: String
    ): String {
        return if (jsonObject.containsKey(field) && !jsonObject.isNull(field))
            jsonObject.getString(field)
        else defaultValue
    }

    private fun readBigDecimal(jsonObject: jakarta.json.JsonObject, field: String): BigDecimal {
        return if (jsonObject.containsKey(field) && !jsonObject.isNull(field))
            jsonObject.getJsonNumber(field).bigDecimalValue()
        else BigDecimal.ZERO
    }

    private fun addDefaultField(builder: JsonObjectBuilder, field: String, defaultValue: Any) {
        when (defaultValue) {
            is String -> builder.add(field, defaultValue)
            is Double -> builder.add(field, defaultValue)
            else -> builder.addNull(field)
        }
    }

    private fun normalizeArticleData(article: Article) {
        if (article.quantity?.compareTo(BigDecimal.ZERO) ?: -1 < 0) {
            article.quantity = BigDecimal.ZERO
        }

        val price = article.price ?: BigDecimal.ZERO
        val quantity = article.quantity ?: BigDecimal.ZERO
        val discount = article.discount ?: BigDecimal.ZERO

        val calculatedTotal = price.multiply(quantity).subtract(discount)
        article.total = calculatedTotal.max(BigDecimal.ZERO)
    }

}
