package dev.lueem.integration.supercard.infra

data class SupercardReceiptLink(
    val receiptUrl: String,
    val externalReceiptId: String,
    val purchaseDate: String? = null,  // ISO date from API, preferred over PDF extraction
    val totalChf: java.math.BigDecimal? = null  // total in CHF from API (amount is in Rappen)
)
