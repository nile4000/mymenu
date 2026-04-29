package dev.lueem.integration.supercard.api.dto

class SupercardSessionRequest {
    var cookieHeader: String = ""
}

data class SupercardStatusResponse(
    val connected: Boolean,
    val sessionUpdatedAt: String?,
)
