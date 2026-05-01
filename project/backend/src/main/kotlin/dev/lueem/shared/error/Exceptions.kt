package dev.lueem.shared.error

// Upload means errors while processing uploaded input through internal AI services.
class UploadCategorizationException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

// Upload means errors while processing uploaded input through external AI services.
class UploadOpenAiException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
