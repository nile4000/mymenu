package dev.lueem.extraction.api

import java.math.BigDecimal

/**
 * JSON response DTO for a single article.
 * Kept separate from the domain Article so the API contract can evolve independently.
 * Serialized by JSON-B using property names directly.
 */
data class ArticleDto(
    val name: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val discount: BigDecimal,
    val total: BigDecimal,
    val category: String
)
