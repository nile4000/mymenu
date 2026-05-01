package dev.lueem.shared.error

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import java.util.logging.Level
import java.util.logging.Logger

@Provider
class GlobalExceptionMapper : ExceptionMapper<Exception> {

    companion object {
        private val LOGGER = Logger.getLogger(GlobalExceptionMapper::class.java.name)
    }

    override fun toResponse(e: Exception): Response {
        return when (e) {
            is WebApplicationException -> e.response
            is IllegalArgumentException -> {
                LOGGER.log(Level.WARNING, "Validation error: ${e.message}")
                errorResponse(422, "VALIDATION_ERROR", e.message ?: "Invalid input")
            }
            is UploadOpenAiException -> {
                LOGGER.log(Level.SEVERE, "Upload OpenAI error", e)
                errorResponse(502, "Upload_ERROR", "Upload AI call failed")
            }
            is UploadCategorizationException -> {
                LOGGER.log(Level.SEVERE, "Upload categorization service error", e)
                errorResponse(502, "Upload_ERROR", "Upload categorization service call failed")
            }
            else -> {
                LOGGER.log(Level.SEVERE, "Unhandled exception", e)
                errorResponse(500, "INTERNAL_ERROR", "An internal error occurred")
            }
        }
    }

    private fun errorResponse(status: Int, code: String, message: String): Response =
        Response.status(status)
            .entity(ApiError(code = code, message = message))
            .build()
}
