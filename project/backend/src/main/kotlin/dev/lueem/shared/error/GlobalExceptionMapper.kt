package dev.lueem.shared.error
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import jakarta.ws.rs.core.Response
import java.util.logging.Level
import java.util.logging.Logger

@Provider
class GlobalExceptionMapper : ExceptionMapper<Exception> {

    companion object {
        private val LOGGER = Logger.getLogger(GlobalExceptionMapper::class.java.name)
    }

    override fun toResponse(e: Exception): Response {
        return when (e) {
            is IllegalArgumentException -> {
                LOGGER.log(Level.WARNING, "Validation error: ${e.message}")
                Response.status(422)
                    .entity(ApiError(code = "VALIDATION_ERROR", message = e.message ?: "Invalid input"))
                    .build()
            }
            is UpstreamOpenAiException -> {
                LOGGER.log(Level.SEVERE, "Upstream OpenAI error", e)
                Response.status(502)
                    .entity(ApiError(code = "UPSTREAM_ERROR", message = "Upstream AI call failed"))
                    .build()
            }
            else -> {
                LOGGER.log(Level.SEVERE, "Unhandled exception", e)
                Response.serverError()
                    .entity(ApiError(code = "INTERNAL_ERROR", message = "An internal error occurred"))
                    .build()
            }
        }
    }
}
