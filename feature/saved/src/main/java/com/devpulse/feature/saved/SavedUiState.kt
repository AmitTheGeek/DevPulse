package com.devpulse.feature.saved

import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.model.Repository

data class SavedUiState(
    val repositories: List<Repository> = emptyList(),
    val isLoading: Boolean = false,
    val updatingRepositoryId: Long? = null,
    val actionError: SavedUiError? = null,
) {
    val isEmpty: Boolean = !isLoading && repositories.isEmpty()
}

sealed interface SavedUiError {
    val message: String

    data object LocalStorageError : SavedUiError {
        override val message = "DevPulse could not update saved repositories."
    }

    data object Unknown : SavedUiError {
        override val message = "DevPulse could not update this saved repository right now."
    }
}

fun DevPulseError.toSavedUiError(): SavedUiError =
    when (this) {
        is DevPulseError.LocalStorageError -> SavedUiError.LocalStorageError
        else -> SavedUiError.Unknown
    }
