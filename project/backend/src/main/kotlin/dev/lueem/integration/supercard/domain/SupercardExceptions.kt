package dev.lueem.integration.supercard.domain

import java.time.Duration
import java.time.Instant

open class SupercardException(
    message: String,
    val httpStatus: Int,
    val code: String,
) : RuntimeException(message)

class SupercardSyncInProgressException : SupercardException(
    message = "Supercard sync is already running",
    httpStatus = 409,
    code = "SUPERCARD_SYNC_IN_PROGRESS",
)

class SupercardCooldownException(
    val retryAt: Instant,
) : SupercardException(
    message = "Supercard requests are paused until $retryAt",
    httpStatus = 429,
    code = "SUPERCARD_COOLDOWN",
)

class SupercardRemoteAccessException(
    message: String,
    val upstreamStatus: Int,
    val retryAfter: Duration? = null,
) : SupercardException(
    message = message,
    httpStatus = if (upstreamStatus == 401 || upstreamStatus == 403) 401 else if (upstreamStatus == 429) 429 else 502,
    code = when (upstreamStatus) {
        401, 403 -> "SUPERCARD_SESSION_INVALID"
        429 -> "SUPERCARD_RATE_LIMITED"
        else -> "SUPERCARD_UPSTREAM_ERROR"
    },
)
