package dev.lueem.integration.supercard.api

class SupercardSessionRequest {
    var cookieHeader: String = ""
}

data class SupercardStatusResponse(
    val connected: Boolean,
    val sessionUpdatedAt: String?
)

data class SupercardSyncResponse(
    val importedReceipts: Int,
    val skippedReceipts: Int,
    val failedReceipts: Int,
    val errors: List<String>
)

class SupercardSyncSingleRequest {
    var receiptUrl: String? = null
    var bc: String? = null
}

data class SupercardAvailableReceipt(
    val receiptUrl: String,
    val externalReceiptId: String,
    val locationName: String?,
    val logoUrl: String?,
    val purchaseDate: String?,
    val totalChf: String?
)

data class SupercardAvailableResponse(
    val count: Int,
    val receipts: List<SupercardAvailableReceipt>
)
