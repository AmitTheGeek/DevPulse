package com.devpulse.feature.repository

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpulse.core.data.error.DataResult
import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.data.repository.RefreshPolicy
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
class RepositoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repositoryCatalog: RepositoryCatalog,
    private val updatedAtFormatter: RepositoryUpdatedAtFormatter,
) : ViewModel() {
    private val owner: String =
        checkNotNull(savedStateHandle[RepositoryDestination.OWNER_ARGUMENT])
    private val repositoryName: String =
        checkNotNull(savedStateHandle[RepositoryDestination.REPOSITORY_NAME_ARGUMENT])

    private val refreshState = MutableStateFlow(RefreshState(isRefreshing = true))
    private val saveState = MutableStateFlow(SaveState())
    private var refreshJob: Job? = null
    private var saveJob: Job? = null

    val uiState: StateFlow<RepositoryUiState> =
        combine(
            repositoryCatalog.observeRepository(owner, repositoryName),
            refreshState,
            saveState,
        ) { repository, refresh, save ->
            val refreshUiError = refresh.error?.toRepositoryUiError()
            RepositoryUiState(
                owner = owner,
                repositoryName = repositoryName,
                repository = repository,
                updatedAtLabel = updatedAtFormatter.format(repository?.updatedAt),
                isRefreshing = refresh.isRefreshing,
                isSaving = save.isSaving,
                initialError = if (repository == null) refreshUiError else null,
                refreshError = if (repository != null) refreshUiError else null,
                saveError = save.error?.toRepositoryUiError(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RepositoryUiState(
                owner = owner,
                repositoryName = repositoryName,
                isRefreshing = true,
            ),
        )

    init {
        refresh(RefreshPolicy.IfStale)
    }

    fun onRefresh() {
        refresh(RefreshPolicy.Force)
    }

    fun onRetry() {
        refresh(RefreshPolicy.Force)
    }

    fun onToggleSaved() {
        val repository = uiState.value.repository ?: return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            saveState.update { SaveState(isSaving = true) }

            val result = repositoryCatalog.setRepositorySaved(
                id = repository.id,
                saved = !repository.isSaved,
            )

            saveState.update {
                when (result) {
                    is DataResult.Success -> SaveState(isSaving = false)
                    is DataResult.Failure -> SaveState(
                        isSaving = false,
                        error = result.error,
                    )
                }
            }
        }
    }

    private fun refresh(refreshPolicy: RefreshPolicy) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            refreshState.update { RefreshState(isRefreshing = true) }

            val result = repositoryCatalog.refreshRepository(
                owner = owner,
                repositoryName = repositoryName,
                refreshPolicy = refreshPolicy,
            )

            refreshState.update {
                when (result) {
                    is DataResult.Success -> RefreshState(isRefreshing = false)
                    is DataResult.Failure -> RefreshState(
                        isRefreshing = false,
                        error = result.error,
                    )
                }
            }
        }
    }

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val error: DevPulseError? = null,
    )

    private data class SaveState(
        val isSaving: Boolean = false,
        val error: DevPulseError? = null,
    )
}
