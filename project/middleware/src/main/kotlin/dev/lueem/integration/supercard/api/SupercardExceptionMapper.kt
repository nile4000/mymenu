package dev.lueem.integration.supercard.api

import dev.lueem.integration.supercard.domain.SupercardCooldownException
import dev.lueem.integration.supercard.domain.SupercardException
import dev.lueem.integration.supercard.domain.SupercardRemoteAccessException
import dev.lueem.shared.error.ApiError
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import java.time.Duration
import java.time.Instant

@Provider
class SupercardExceptionMapper : ExceptionMapper<SupercardException> {

    override fun toResponse(e: SupercardException): Response {
        val builder = Response.status(e.httpStatus)
            .entity(ApiError(code = e.code, message = e.message ?: "Supercard integration failed"))

        if (e is SupercardCooldownException) {
            val retryAfterSeconds = Duration.between(Instant.now(), e.retryAt).seconds.coerceAtLeast(0)
            builder.header("Retry-After", retryAfterSeconds.toString())
        }
        if (e is SupercardRemoteAccessException && e.retryAfter != null) {
            builder.header("Retry-After", e.retryAfter.seconds.coerceAtLeast(0).toString())
        }

        return builder.build()
    }
}
