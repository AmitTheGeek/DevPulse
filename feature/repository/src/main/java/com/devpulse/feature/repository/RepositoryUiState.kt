package com.devpulse.feature.repository

import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.model.Repository

data class RepositoryUiState(
    val owner: String,
    val repositoryName: String,
    val repository: Repository? = null,
    val updatedAtLabel: String = "Updated time unavailable",
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val initialError: RepositoryUiError? = null,
    val refreshError: RepositoryUiError? = null,
    val saveError: RepositoryUiError? = null,
) {
    val hasContent: Boolean = repository != null
    val isInitialLoading: Boolean = repository == null && isRefreshing && initialError == null
    val isInitialError: Boolean = repository == null && initialError != null
}

sealed interface RepositoryUiError {
    val title: String
    val message: String

    data object NetworkUnavailable : RepositoryUiError {
        override val title = "No connection"
        override val message = "Repository data is unavailable while the network is offline."
    }

    data object NotFound : RepositoryUiError {
        override val title = "Repository not found"
        override val message = "GitHub could not find this repository."
    }

    data class RateLimited(
        val resetEpochSeconds: Long?,
    ) : RepositoryUiError {
        override val title = "GitHub rate limit reached"
        override val message = if (resetEpochSeconds == null) {
            "GitHub has paused unauthenticated refreshes. Try again later."
        } else {
            "GitHub has paused unauthenticated refreshes until reset time $resetEpochSeconds."
        }
    }

    data object ServerError : RepositoryUiError {
        override val title = "GitHub is unavailable"
        override val message = "GitHub returned a server error. Your cached data is still available."
    }

    data object LocalStorageError : RepositoryUiError {
        override val title = "Local storage problem"
        override val message = "DevPulse could not update local repository storage."
    }

    data object Unknown : RepositoryUiError {
        override val title = "Something went wrong"
        override val message = "DevPulse could not update this repository right now."
    }
}

fun DevPulseError.toRepositoryUiError(): RepositoryUiError =
    when (this) {
        DevPulseError.NetworkUnavailable -> RepositoryUiError.NetworkUnavailable
        DevPulseError.NotFound -> RepositoryUiError.NotFound
        is DevPulseError.RateLimited -> RepositoryUiError.RateLimited(resetEpochSeconds)
        is DevPulseError.ServerError -> RepositoryUiError.ServerError
        is DevPulseError.LocalStorageError -> RepositoryUiError.LocalStorageError
        is DevPulseError.Unknown -> RepositoryUiError.Unknown
    }
