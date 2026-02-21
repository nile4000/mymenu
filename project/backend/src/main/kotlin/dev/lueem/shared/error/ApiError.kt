package dev.lueem.shared.error

data class ApiError(
    val code: String,
    val message: String,
    val traceId: String? = null
)
