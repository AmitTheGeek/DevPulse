package com.devpulse.core.data.error

import android.database.SQLException
import java.io.IOException
import retrofit2.HttpException

internal fun Throwable.toDevPulseError(): DevPulseError =
    when (this) {
        is IOException -> DevPulseError.NetworkUnavailable
        is HttpException -> toHttpError()
        is SQLException -> DevPulseError.LocalStorageError(this)
        else -> DevPulseError.Unknown(this)
    }

private fun HttpException.toHttpError(): DevPulseError {
    val response = response()
    val statusCode = code()
    val message = response?.message()?.takeIf { it.isNotBlank() } ?: message()

    return when {
        statusCode == 404 -> DevPulseError.NotFound
        statusCode == 429 || isGitHubRateLimit(response?.headers()?.get("X-RateLimit-Remaining")) -> {
            DevPulseError.RateLimited(
                statusCode = statusCode,
                resetEpochSeconds = response?.headers()?.get("X-RateLimit-Reset")?.toLongOrNull(),
                message = message,
            )
        }
        statusCode in 500..599 -> DevPulseError.ServerError(statusCode = statusCode, message = message)
        else -> DevPulseError.Unknown(this)
    }
}

private fun isGitHubRateLimit(remainingHeader: String?): Boolean = remainingHeader == "0"

