package dev.lueem.service

import jakarta.json.Json
import jakarta.json.JsonValue
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ArticleJsonMapperTest {

    private val mapper = ArticleJsonMapper()

    @Test
    fun sanitizeArticleJsonArray_fillsMissingFieldsAndSkipsNonObjects() {
        val input =
            Json.createArrayBuilder()
                .add(
                    Json.createObjectBuilder()
                        .add(ArticleJsonSchema.NAME_FIELD, "Milk")
                        .add(ArticleJsonSchema.PRICE_FIELD, 1.5)
                )
                .add(JsonValue.TRUE)
                .build()

        val result = mapper.sanitizeArticleJsonArray(input)

        assertEquals(1, result.size)
        val obj = result.getJsonObject(0)
        assertEquals("Milk", obj.getString(ArticleJsonSchema.NAME_FIELD))
        assertBigDecimalEquals(
            BigDecimal("1.5"),
            obj.getJsonNumber(ArticleJsonSchema.PRICE_FIELD).bigDecimalValue()
        )
        assertBigDecimalEquals(
            BigDecimal.ZERO,
            obj.getJsonNumber(ArticleJsonSchema.QUANTITY_FIELD).bigDecimalValue()
        )
        assertBigDecimalEquals(
            BigDecimal.ZERO,
            obj.getJsonNumber(ArticleJsonSchema.DISCOUNT_FIELD).bigDecimalValue()
        )
        assertBigDecimalEquals(
            BigDecimal.ZERO,
            obj.getJsonNumber(ArticleJsonSchema.TOTAL_FIELD).bigDecimalValue()
        )
    }

    @Test
    fun mapToArticles_mapsAndNormalizesValues() {
        val input =
            Json.createArrayBuilder()
                .add(
                    Json.createObjectBuilder()
                        .add(ArticleJsonSchema.NAME_FIELD, "Test")
                        .add(ArticleJsonSchema.PRICE_FIELD, 2.0)
                        .add(ArticleJsonSchema.QUANTITY_FIELD, -1.0)
                        .add(ArticleJsonSchema.DISCOUNT_FIELD, 1.0)
                        .add(ArticleJsonSchema.TOTAL_FIELD, 999.0)
                )
                .build()

        val articles = mapper.mapToArticles(input)

        assertEquals(1, articles.size)
        val article = articles[0]
        assertEquals("Test", article.name)
        assertNotNull(article.price)
        assertNotNull(article.quantity)
        assertNotNull(article.discount)
        assertNotNull(article.total)
        assertEquals("", article.category)

        assertBigDecimalEquals(BigDecimal("2.0"), article.price!!)
        assertBigDecimalEquals(BigDecimal.ZERO, article.quantity!!)
        assertBigDecimalEquals(BigDecimal("1.0"), article.discount!!)
        assertBigDecimalEquals(BigDecimal.ZERO, article.total!!)
    }

    private fun assertBigDecimalEquals(expected: BigDecimal, actual: BigDecimal) {
        assertEquals(0, expected.compareTo(actual))
    }
}
