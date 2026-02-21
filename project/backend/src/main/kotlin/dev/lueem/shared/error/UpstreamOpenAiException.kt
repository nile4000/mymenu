package dev.lueem.shared.error

class UpstreamOpenAiException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
