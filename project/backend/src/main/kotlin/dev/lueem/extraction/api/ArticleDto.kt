package dev.lueem.extraction.api

import java.math.BigDecimal

/**
 * JSON response DTO for a single extracted article.
 */
data class ArticleDto(
    val name: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val discount: BigDecimal,
    val total: BigDecimal,
    val category: String
)
