package dev.lueem.integration.supercard.api.dto

data class SupercardAvailableReceipt(
    val receiptUrl: String,
    val supercardReceiptBarcode: String,
    val locationName: String?,
    val logoUrl: String?,
    val purchaseDate: String?,
    val totalChf: String?,
)

data class SupercardAvailableResponse(
    val count: Int,
    val receipts: List<SupercardAvailableReceipt>,
)
