package com.devpulse.feature.developer

import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.model.Developer
import com.devpulse.core.model.Repository

data class DeveloperUiState(
    val username: String,
    val developer: Developer? = null,
    val repositories: List<Repository> = emptyList(),
    val isRefreshing: Boolean = false,
    val initialError: DeveloperUiError? = null,
    val refreshError: DeveloperUiError? = null,
) {
    val hasContent: Boolean = developer != null
    val isInitialLoading: Boolean = !hasContent && isRefreshing && initialError == null
    val isInitialError: Boolean = !hasContent && !isRefreshing && initialError != null
    val isRepositoryListEmpty: Boolean = hasContent && repositories.isEmpty() && !isRefreshing
}

sealed interface DeveloperUiError {
    val title: String
    val message: String

    data object NetworkUnavailable : DeveloperUiError {
        override val title: String = "No connection"
        override val message: String = "Cached data stays available. Try again when the network is back."
    }

    data object NotFound : DeveloperUiError {
        override val title: String = "Developer not found"
        override val message: String = "Check the username and try another search."
    }

    data class RateLimited(
        val resetEpochSeconds: Long?,
    ) : DeveloperUiError {
        override val title: String = "GitHub rate limit reached"
        override val message: String = "DevPulse can try again after GitHub opens the unauthenticated limit."
    }

    data object ServerError : DeveloperUiError {
        override val title: String = "GitHub is having trouble"
        override val message: String = "Your cached data is still available. Try refreshing again shortly."
    }

    data object LocalStorageError : DeveloperUiError {
        override val title: String = "Local storage failed"
        override val message: String = "DevPulse could not read or update its local cache."
    }

    data object Unknown : DeveloperUiError {
        override val title: String = "Something went wrong"
        override val message: String = "DevPulse could not finish refreshing this developer."
    }
}

fun DevPulseError.toDeveloperUiError(): DeveloperUiError =
    when (this) {
        DevPulseError.NetworkUnavailable -> DeveloperUiError.NetworkUnavailable
        DevPulseError.NotFound -> DeveloperUiError.NotFound
        is DevPulseError.RateLimited -> DeveloperUiError.RateLimited(
            resetEpochSeconds = resetEpochSeconds,
        )
        is DevPulseError.ServerError -> DeveloperUiError.ServerError
        is DevPulseError.LocalStorageError -> DeveloperUiError.LocalStorageError
        is DevPulseError.Unknown -> DeveloperUiError.Unknown
    }
