package dev.lueem.model

import java.math.BigDecimal

data class ReceiptResponse(
        val uid: String,
        val purchaseDate: String,
        val corp: String,
        val total: BigDecimal,
        val articles: List<Article>,
        val metadata: ReceiptMetadata
)

data class ReceiptMetadata(val extractedTotalRow: Int, val openAiArticleCount: Int)
