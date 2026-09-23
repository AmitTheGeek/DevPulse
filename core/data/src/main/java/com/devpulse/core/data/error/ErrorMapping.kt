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
        statusCode == HTTP_NOT_FOUND -> DevPulseError.NotFound
        statusCode == HTTP_TOO_MANY_REQUESTS ||
            isGitHubRateLimit(response?.headers()?.get("X-RateLimit-Remaining")) -> {
            DevPulseError.RateLimited(
                statusCode = statusCode,
                resetEpochSeconds = response?.headers()?.get("X-RateLimit-Reset")?.toLongOrNull(),
                message = message,
            )
        }
        statusCode in HTTP_SERVER_ERROR_MIN..HTTP_SERVER_ERROR_MAX -> {
            DevPulseError.ServerError(statusCode = statusCode, message = message)
        }
        else -> DevPulseError.Unknown(this)
    }
}

private fun isGitHubRateLimit(remainingHeader: String?): Boolean = remainingHeader == "0"

private const val HTTP_NOT_FOUND = 404
private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_SERVER_ERROR_MIN = 500
private const val HTTP_SERVER_ERROR_MAX = 599
