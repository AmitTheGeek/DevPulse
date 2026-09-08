package com.devpulse.core.data.error

sealed interface DevPulseError {
    data object NetworkUnavailable : DevPulseError
    data object NotFound : DevPulseError
    data object RateLimited : DevPulseError
    data class ServerError(val statusCode: Int? = null, val message: String? = null) : DevPulseError
    data class LocalStorageError(val cause: Throwable? = null) : DevPulseError
    data class Unknown(val cause: Throwable? = null) : DevPulseError
}

