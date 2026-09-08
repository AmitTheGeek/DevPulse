package com.devpulse.core.data.error

sealed interface DataResult<out T> {
    data class Success<T>(val value: T) : DataResult<T>
    data class Failure(val error: DevPulseError) : DataResult<Nothing>
}

