package com.devpulse.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpulse.core.data.error.DataResult
import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.data.repository.RepositoryCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repositoryCatalog: RepositoryCatalog,
) : ViewModel() {
    private val actionState = MutableStateFlow(ActionState())
    private var unsaveJob: Job? = null

    val uiState: StateFlow<SavedUiState> =
        combine(
            repositoryCatalog.observeSavedRepositories(),
            actionState,
        ) { repositories, action ->
            SavedUiState(
                repositories = repositories,
                isLoading = false,
                updatingRepositoryId = action.updatingRepositoryId,
                actionError = action.error?.toSavedUiError(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SavedUiState(isLoading = true),
        )

    fun onUnsave(repositoryId: Long) {
        unsaveJob?.cancel()
        unsaveJob = viewModelScope.launch {
            actionState.update {
                ActionState(updatingRepositoryId = repositoryId)
            }

            val result = repositoryCatalog.setRepositorySaved(
                id = repositoryId,
                saved = false,
            )

            actionState.update {
                when (result) {
                    is DataResult.Success -> ActionState()
                    is DataResult.Failure -> ActionState(error = result.error)
                }
            }
        }
    }

    private data class ActionState(
        val updatingRepositoryId: Long? = null,
        val error: DevPulseError? = null,
    )
}
