package com.devpulse.core.data.error

import kotlinx.coroutines.CancellationException

@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> dataResultOf(
    mapException: (Exception) -> DevPulseError = { exception -> exception.toDevPulseError() },
    block: suspend () -> T,
): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        DataResult.Failure(mapException(exception))
    }
