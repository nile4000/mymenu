package dev.lueem.extraction.api

import java.math.BigDecimal

data class ReceiptResponse(
    val uid: String,
    val purchaseDate: String,
    val corp: String,
    val total: BigDecimal,
    val articles: List<ArticleDto>,
    val metadata: ReceiptMetadata,
    val barcode: String? = null
)

data class ReceiptMetadata(
    val extractedTotalRow: Int,
    val openAiArticleCount: Int
)
