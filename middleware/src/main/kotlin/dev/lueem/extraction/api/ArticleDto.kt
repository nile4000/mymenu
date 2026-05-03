package dev.lueem.extraction.api

import dev.lueem.extraction.domain.Article
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

fun Article.toDto() = ArticleDto(
    name = name,
    price = price,
    quantity = quantity,
    discount = discount,
    total = total,
    category = category
)
