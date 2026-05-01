package dev.lueem.integration.supercard.api.dto

data class SupercardSyncResponse(
    val importedReceipts: Int,
    val deferredReceipts: Int,
    val failedReceipts: Int,
    val errors: List<String>,
)
