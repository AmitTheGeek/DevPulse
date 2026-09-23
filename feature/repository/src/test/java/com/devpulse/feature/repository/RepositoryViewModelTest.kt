package com.devpulse.feature.repository

import androidx.lifecycle.SavedStateHandle
import com.devpulse.core.data.error.DataResult
import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.data.repository.RefreshPolicy
import com.devpulse.core.data.repository.RepositoryCatalog
import com.devpulse.core.model.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cachedRepositoryAppearsImmediately() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            initialRepository = sampleRepository(),
        )
        val viewModel = newViewModel(repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertEquals("Hello-World", viewModel.uiState.value.repository?.name)
        assertEquals("Updated Sep 8, 2026", viewModel.uiState.value.updatedAtLabel)
        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun staleAwareRefreshIsRequestedOnOpen() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog()
        val viewModel = newViewModel(repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertEquals(listOf(RefreshPolicy.IfStale), repositoryCatalog.refreshPolicies)
    }

    @Test
    fun successfulRefreshUpdatesThroughRepositoryFlow() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            refreshBlock = {
                repository.value = sampleRepository(starCount = 120)
                DataResult.Success(Unit)
            },
        )
        val viewModel = newViewModel(repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertEquals(120, viewModel.uiState.value.repository?.starCount)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun refreshFailurePreservesCachedContent() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            initialRepository = sampleRepository(),
            refreshResult = DataResult.Failure(DevPulseError.NetworkUnavailable),
        )
        val viewModel = newViewModel(repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertEquals("Hello-World", viewModel.uiState.value.repository?.name)
        assertTrue(viewModel.uiState.value.refreshError is RepositoryUiError.NetworkUnavailable)
        assertNull(viewModel.uiState.value.initialError)
    }

    @Test
    fun rateLimitMapsToPresentationState() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            refreshResult = DataResult.Failure(
                DevPulseError.RateLimited(
                    statusCode = 403,
                    resetEpochSeconds = 1_788_859_800L,
                ),
            ),
        )
        val viewModel = newViewModel(repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        val error = viewModel.uiState.value.initialError
        assertTrue(error is RepositoryUiError.RateLimited)
        error as RepositoryUiError.RateLimited
        assertEquals(1_788_859_800L, error.resetEpochSeconds)
    }

    @Test
    fun saveActionUpdatesObservedSavedState() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            initialRepository = sampleRepository(isSaved = false),
        )
        val viewModel = newViewModel(repositoryCatalog)
        collectState(viewModel)
        advanceUntilIdle()

        viewModel.onToggleSaved()
        advanceUntilIdle()

        assertEquals(listOf(1296269L to true), repositoryCatalog.savedRequests)
        assertTrue(viewModel.uiState.value.repository?.isSaved == true)
    }

    @Test
    fun unsaveActionUpdatesObservedSavedState() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            initialRepository = sampleRepository(isSaved = true),
        )
        val viewModel = newViewModel(repositoryCatalog)
        collectState(viewModel)
        advanceUntilIdle()

        viewModel.onToggleSaved()
        advanceUntilIdle()

        assertEquals(listOf(1296269L to false), repositoryCatalog.savedRequests)
        assertFalse(viewModel.uiState.value.repository?.isSaved == true)
    }

    @Test
    fun savePersistenceFailureIsRepresented() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            initialRepository = sampleRepository(isSaved = false),
            setSavedResult = DataResult.Failure(DevPulseError.LocalStorageError()),
        )
        val viewModel = newViewModel(repositoryCatalog)
        collectState(viewModel)
        advanceUntilIdle()

        viewModel.onToggleSaved()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveError is RepositoryUiError.LocalStorageError)
        assertFalse(viewModel.uiState.value.repository?.isSaved == true)
    }

    private fun TestScope.collectState(viewModel: RepositoryViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
    }

    private fun newViewModel(repositoryCatalog: RepositoryCatalog): RepositoryViewModel =
        RepositoryViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    RepositoryDestination.OWNER_ARGUMENT to "octocat",
                    RepositoryDestination.REPOSITORY_NAME_ARGUMENT to "Hello-World",
                ),
            ),
            repositoryCatalog = repositoryCatalog,
            updatedAtFormatter = RepositoryUpdatedAtFormatter(),
        )

    private class FakeRepositoryCatalog(
        initialRepository: Repository? = null,
        private val refreshResult: DataResult<Unit> = DataResult.Success(Unit),
        private val setSavedResult: DataResult<Unit> = DataResult.Success(Unit),
        private val refreshBlock: (suspend FakeRepositoryCatalog.(RefreshPolicy) -> DataResult<Unit>)? = null,
    ) : RepositoryCatalog {
        val repository = MutableStateFlow(initialRepository)
        val refreshPolicies = mutableListOf<RefreshPolicy>()
        val savedRequests = mutableListOf<Pair<Long, Boolean>>()

        override fun observeRepositories(username: String): Flow<List<Repository>> =
            MutableStateFlow(emptyList())

        override fun observeRepository(owner: String, repositoryName: String): Flow<Repository?> =
            repository

        override suspend fun refreshRepositories(
            username: String,
            refreshPolicy: RefreshPolicy,
        ): DataResult<Unit> = DataResult.Success(Unit)

        override suspend fun refreshRepository(
            owner: String,
            repositoryName: String,
            refreshPolicy: RefreshPolicy,
        ): DataResult<Unit> {
            refreshPolicies += refreshPolicy
            return refreshBlock?.invoke(this, refreshPolicy) ?: refreshResult
        }

        override suspend fun setRepositorySaved(
            id: Long,
            saved: Boolean,
        ): DataResult<Unit> {
            savedRequests += id to saved
            if (setSavedResult is DataResult.Success) {
                repository.update { current -> current?.copy(isSaved = saved) }
            }
            return setSavedResult
        }

        override fun observeSavedRepositories(): Flow<List<Repository>> =
            MutableStateFlow(repository.value?.takeIf { it.isSaved }?.let(::listOf) ?: emptyList())
    }

    class MainDispatcherRule(
        private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }

    private fun sampleRepository(
        starCount: Int = 80,
        isSaved: Boolean = false,
    ): Repository =
        Repository(
            id = 1296269L,
            ownerUsername = "octocat",
            name = "Hello-World",
            fullName = "octocat/Hello-World",
            description = "My first repository on GitHub.",
            url = "https://github.com/octocat/Hello-World",
            primaryLanguage = "Kotlin",
            starCount = starCount,
            forkCount = 9,
            openIssueCount = 3,
            isFork = false,
            isArchived = false,
            isPrivate = false,
            isSaved = isSaved,
            updatedAt = Instant.parse("2026-09-08T09:30:00Z"),
        )
}
