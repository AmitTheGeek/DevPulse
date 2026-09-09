package com.devpulse.feature.developer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpulse.core.data.error.DataResult
import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.data.repository.DeveloperRepository
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
class DeveloperViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val developerRepository: DeveloperRepository,
    private val repositoryCatalog: RepositoryCatalog,
) : ViewModel() {
    private val username: String =
        checkNotNull(savedStateHandle[DeveloperDestination.USERNAME_ARGUMENT])

    private val refreshState = MutableStateFlow(
        RefreshState(isRefreshing = true),
    )
    private var refreshJob: Job? = null

    val uiState: StateFlow<DeveloperUiState> =
        combine(
            developerRepository.observeDeveloper(username),
            repositoryCatalog.observeRepositories(username),
            refreshState,
        ) { developer, repositories, refresh ->
            val uiError = refresh.error?.toDeveloperUiError()
            DeveloperUiState(
                username = username,
                developer = developer,
                repositories = repositories,
                isRefreshing = refresh.isRefreshing,
                initialError = if (developer == null) uiError else null,
                refreshError = if (developer != null) uiError else null,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DeveloperUiState(
                username = username,
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

    private fun refresh(refreshPolicy: RefreshPolicy) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            refreshState.update {
                RefreshState(isRefreshing = true)
            }

            val result = developerRepository.refreshDeveloper(
                username = username,
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
}
