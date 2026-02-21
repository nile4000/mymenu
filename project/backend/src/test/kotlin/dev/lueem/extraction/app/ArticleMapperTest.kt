package dev.lueem.extraction.app

import jakarta.json.Json
import jakarta.json.JsonValue
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ArticleMapperTest {

    private val mapper = ArticleMapper()

    @Test
    fun sanitizeArticleJsonArray_fillsMissingFieldsAndSkipsNonObjects() {
        val input = Json.createArrayBuilder()
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
        val input = Json.createArrayBuilder()
            .add(
                Json.createObjectBuilder()
                    .add(ArticleJsonSchema.NAME_FIELD, "Test")
                    .add(ArticleJsonSchema.PRICE_FIELD, 2.0)
                    .add(ArticleJsonSchema.QUANTITY_FIELD, -1.0)   // negative → clamped to 0
                    .add(ArticleJsonSchema.DISCOUNT_FIELD, 1.0)
                    .add(ArticleJsonSchema.TOTAL_FIELD, 999.0)    // overridden by calculation
            )
            .build()

        val articles = mapper.mapToArticles(input)

        assertEquals(1, articles.size)
        val article = articles[0]
        assertEquals("Test", article.name)
        assertBigDecimalEquals(BigDecimal("2.0"), article.price)
        assertBigDecimalEquals(BigDecimal.ZERO, article.quantity)      // clamped
        assertBigDecimalEquals(BigDecimal("1.0"), article.discount)
        assertBigDecimalEquals(BigDecimal.ZERO, article.total)         // 2*0 - 1 = -1 → 0
        assertEquals("", article.category)
    }

    @Test
    fun toDto_mapsAllFieldsCorrectly() {
        val input = Json.createArrayBuilder()
            .add(
                Json.createObjectBuilder()
                    .add(ArticleJsonSchema.NAME_FIELD, "Butter")
                    .add(ArticleJsonSchema.PRICE_FIELD, 3.0)
                    .add(ArticleJsonSchema.QUANTITY_FIELD, 2.0)
                    .add(ArticleJsonSchema.DISCOUNT_FIELD, 0.0)
                    .add(ArticleJsonSchema.TOTAL_FIELD, 6.0)
            )
            .build()

        val articles = mapper.mapToArticles(input)
        val dtos = articles.map { it.toDto() }

        assertEquals(1, dtos.size)
        val dto = dtos[0]
        assertEquals("Butter", dto.name)
        assertBigDecimalEquals(BigDecimal("3.0"), dto.price)
        assertBigDecimalEquals(BigDecimal("2.0"), dto.quantity)
        assertBigDecimalEquals(BigDecimal.ZERO, dto.discount)
        assertBigDecimalEquals(BigDecimal("6.0"), dto.total)
        assertEquals("", dto.category)
    }

    private fun assertBigDecimalEquals(expected: BigDecimal, actual: BigDecimal) =
        assertEquals(0, expected.compareTo(actual))
}
