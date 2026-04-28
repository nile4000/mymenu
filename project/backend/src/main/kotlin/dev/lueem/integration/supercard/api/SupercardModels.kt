package dev.lueem.integration.supercard.api

class SupercardSessionRequest {
    var cookieHeader: String = ""
    var supercardName: String? = null
}

data class SupercardStatusResponse(
    val connected: Boolean,
    val supercardName: String?,
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
    val externalReceiptId: String
)

data class SupercardAvailableResponse(
    val count: Int,
    val receipts: List<SupercardAvailableReceipt>
)
