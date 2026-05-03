package dev.lueem.integration.supercard.domain

import java.math.BigDecimal

data class SupercardReceiptLink(
    val receiptUrl: String,
    val supercardReceiptBarcode: String,
    val locationName: String? = null,
    val logoUrl: String? = null,
    val purchaseDate: String? = null,
    val totalChf: BigDecimal? = null,
)
