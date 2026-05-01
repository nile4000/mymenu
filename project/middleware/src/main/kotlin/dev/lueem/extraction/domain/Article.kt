package dev.lueem.extraction.domain

import java.math.BigDecimal

/**
 * Domain object representing a single article on a receipt.
 * Mapping to/from JSON happens at the api layer.
 */
data class Article(
    val name: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val discount: BigDecimal,
    val total: BigDecimal,
    val category: String = ""
)
