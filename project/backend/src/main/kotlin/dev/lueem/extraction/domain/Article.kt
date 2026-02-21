package dev.lueem.extraction.domain

import java.math.BigDecimal

/**
 * Immutable domain object representing a single article on a receipt.
 * No serialization annotations — mapping to/from JSON happens at the api layer.
 */
data class Article(
    val name: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val discount: BigDecimal,
    val total: BigDecimal,
    val category: String = ""
)
