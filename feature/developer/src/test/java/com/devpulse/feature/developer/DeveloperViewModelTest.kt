package com.devpulse.feature.developer

import androidx.lifecycle.SavedStateHandle
import com.devpulse.core.data.error.DataResult
import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.data.repository.DeveloperRepository
import com.devpulse.core.data.repository.RefreshPolicy
import com.devpulse.core.data.repository.RepositoryCatalog
import com.devpulse.core.model.Developer
import com.devpulse.core.model.Repository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
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
class DeveloperViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cachedDeveloperEmitsContentImmediately() = runTest {
        val developerRepository = FakeDeveloperRepository(
            initialDeveloper = sampleDeveloper(),
        )
        val repositoryCatalog = FakeRepositoryCatalog()
        val viewModel = newViewModel(developerRepository, repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertEquals("octocat", viewModel.uiState.value.developer?.username)
        assertFalse(viewModel.uiState.value.isInitialLoading)
        assertEquals(listOf(RefreshPolicy.IfStale), developerRepository.refreshPolicies)
    }

    @Test
    fun repositoryDataJoinsScreenState() = runTest {
        val developerRepository = FakeDeveloperRepository(
            initialDeveloper = sampleDeveloper(),
        )
        val repositoryCatalog = FakeRepositoryCatalog(
            initialRepositories = listOf(sampleRepository()),
        )
        val viewModel = newViewModel(developerRepository, repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertEquals("Hello-World", viewModel.uiState.value.repositories.single().name)
    }

    @Test
    fun initialLoadWithoutCacheShowsLoading() = runTest {
        val refreshResult = CompletableDeferred<DataResult<Unit>>()
        val developerRepository = FakeDeveloperRepository(
            refreshBlock = { refreshResult.await() },
        )
        val repositoryCatalog = FakeRepositoryCatalog()
        val viewModel = newViewModel(developerRepository, repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isInitialLoading)
        assertNull(viewModel.uiState.value.initialError)

        refreshResult.complete(DataResult.Success(Unit))
        advanceUntilIdle()
    }

    @Test
    fun successfulRefreshUpdatesViaRepositoryFlow() = runTest {
        lateinit var repositoryCatalog: FakeRepositoryCatalog
        val developerRepository = FakeDeveloperRepository(
            refreshBlock = {
                developer.value = sampleDeveloper(displayName = "Updated Octocat")
                repositoryCatalog.repositories.value = listOf(sampleRepository(name = "Updated-Repo"))
                DataResult.Success(Unit)
            },
        )
        repositoryCatalog = FakeRepositoryCatalog()
        val viewModel = newViewModel(developerRepository, repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertEquals("Updated Octocat", viewModel.uiState.value.developer?.displayName)
        assertEquals("Updated-Repo", viewModel.uiState.value.repositories.single().name)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun refreshFailureRetainsCachedContent() = runTest {
        val developerRepository = FakeDeveloperRepository(
            initialDeveloper = sampleDeveloper(),
            refreshResult = DataResult.Failure(DevPulseError.NetworkUnavailable),
        )
        val repositoryCatalog = FakeRepositoryCatalog(
            initialRepositories = listOf(sampleRepository()),
        )
        val viewModel = newViewModel(developerRepository, repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertEquals("octocat", viewModel.uiState.value.developer?.username)
        assertEquals("Hello-World", viewModel.uiState.value.repositories.single().name)
        assertTrue(viewModel.uiState.value.refreshError is DeveloperUiError.NetworkUnavailable)
        assertNull(viewModel.uiState.value.initialError)
    }

    @Test
    fun initialFailureWithoutCacheProducesError() = runTest {
        val developerRepository = FakeDeveloperRepository(
            refreshResult = DataResult.Failure(DevPulseError.NotFound),
        )
        val repositoryCatalog = FakeRepositoryCatalog()
        val viewModel = newViewModel(developerRepository, repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isInitialError)
        assertTrue(viewModel.uiState.value.initialError is DeveloperUiError.NotFound)
        assertNull(viewModel.uiState.value.developer)
    }

    @Test
    fun rateLimitMapsToPresentationState() = runTest {
        val developerRepository = FakeDeveloperRepository(
            refreshResult = DataResult.Failure(
                DevPulseError.RateLimited(
                    statusCode = 403,
                    resetEpochSeconds = 1_788_859_800L,
                ),
            ),
        )
        val repositoryCatalog = FakeRepositoryCatalog()
        val viewModel = newViewModel(developerRepository, repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        val error = viewModel.uiState.value.initialError
        assertTrue(error is DeveloperUiError.RateLimited)
        error as DeveloperUiError.RateLimited
        assertEquals(1_788_859_800L, error.resetEpochSeconds)
    }

    @Test
    fun manualRefreshInvokesForcedSynchronization() = runTest {
        val developerRepository = FakeDeveloperRepository(
            initialDeveloper = sampleDeveloper(),
        )
        val repositoryCatalog = FakeRepositoryCatalog()
        val viewModel = newViewModel(developerRepository, repositoryCatalog)
        collectState(viewModel)
        advanceUntilIdle()

        viewModel.onRefresh()
        advanceUntilIdle()

        assertEquals(
            listOf(RefreshPolicy.IfStale, RefreshPolicy.Force),
            developerRepository.refreshPolicies,
        )
    }

    private fun TestScope.collectState(viewModel: DeveloperViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
    }

    private fun newViewModel(
        developerRepository: DeveloperRepository,
        repositoryCatalog: RepositoryCatalog,
    ): DeveloperViewModel =
        DeveloperViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(DeveloperDestination.USERNAME_ARGUMENT to "octocat"),
            ),
            developerRepository = developerRepository,
            repositoryCatalog = repositoryCatalog,
        )

    private class FakeDeveloperRepository(
        initialDeveloper: Developer? = null,
        private val refreshResult: DataResult<Unit> = DataResult.Success(Unit),
        private val refreshBlock: (suspend FakeDeveloperRepository.(RefreshPolicy) -> DataResult<Unit>)? = null,
    ) : DeveloperRepository {
        val developer = MutableStateFlow(initialDeveloper)
        val refreshPolicies = mutableListOf<RefreshPolicy>()

        override fun observeDeveloper(username: String): Flow<Developer?> = developer

        override suspend fun refreshDeveloper(
            username: String,
            refreshPolicy: RefreshPolicy,
        ): DataResult<Unit> {
            refreshPolicies += refreshPolicy
            return refreshBlock?.invoke(this, refreshPolicy) ?: refreshResult
        }
    }

    private class FakeRepositoryCatalog(
        initialRepositories: List<Repository> = emptyList(),
    ) : RepositoryCatalog {
        val repositories = MutableStateFlow(initialRepositories)

        override fun observeRepositories(username: String): Flow<List<Repository>> =
            repositories

        override fun observeRepository(
            owner: String,
            repositoryName: String,
        ): Flow<Repository?> =
            MutableStateFlow(repositories.value.firstOrNull { it.name == repositoryName })

        override suspend fun refreshRepositories(
            username: String,
            refreshPolicy: RefreshPolicy,
        ): DataResult<Unit> = DataResult.Success(Unit)

        override suspend fun refreshRepository(
            owner: String,
            repositoryName: String,
            refreshPolicy: RefreshPolicy,
        ): DataResult<Unit> = DataResult.Success(Unit)

        override suspend fun setRepositorySaved(
            id: Long,
            saved: Boolean,
        ): DataResult<Unit> = DataResult.Success(Unit)

        override fun observeSavedRepositories(): Flow<List<Repository>> =
            MutableStateFlow(repositories.value.filter { it.isSaved })
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

    private fun sampleDeveloper(
        displayName: String = "The Octocat",
    ): Developer =
        Developer(
            id = 42L,
            username = "octocat",
            displayName = displayName,
            avatarUrl = "https://avatars.githubusercontent.com/u/42?v=4",
            profileUrl = "https://github.com/octocat",
            bio = "GitHub mascot",
            publicRepositoryCount = 8,
            followerCount = 9001,
            followingCount = 12,
        )

    private fun sampleRepository(
        name: String = "Hello-World",
    ): Repository =
        Repository(
            id = 1296269L,
            ownerUsername = "octocat",
            name = name,
            fullName = "octocat/$name",
            description = "My first repository on GitHub.",
            url = "https://github.com/octocat/$name",
            primaryLanguage = "Kotlin",
            starCount = 80,
            forkCount = 9,
            openIssueCount = 3,
            isFork = false,
            isArchived = false,
            isPrivate = false,
            isSaved = false,
            updatedAt = Instant.parse("2026-09-08T09:30:00Z"),
        )
}
