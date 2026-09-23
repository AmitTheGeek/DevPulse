package com.devpulse.feature.saved

import com.devpulse.core.data.error.DataResult
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun savedRepositoriesAreEmitted() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            initialSavedRepositories = listOf(sampleRepository()),
        )
        val viewModel = SavedViewModel(repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertEquals("Hello-World", viewModel.uiState.value.repositories.single().name)
        assertTrue(repositoryCatalog.refreshRequests.isEmpty())
    }

    @Test
    fun emptySavedState() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog()
        val viewModel = SavedViewModel(repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun unsaveRemovesItemThroughObservedFlow() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            initialSavedRepositories = listOf(sampleRepository()),
        )
        val viewModel = SavedViewModel(repositoryCatalog)
        collectState(viewModel)
        advanceUntilIdle()

        viewModel.onUnsave(1296269L)
        advanceUntilIdle()

        assertEquals(listOf(1296269L to false), repositoryCatalog.savedRequests)
        assertTrue(viewModel.uiState.value.repositories.isEmpty())
    }

    @Test
    fun openingSavedPerformsNoRequiredNetworkRefresh() = runTest {
        val repositoryCatalog = FakeRepositoryCatalog(
            initialSavedRepositories = listOf(sampleRepository()),
        )
        val viewModel = SavedViewModel(repositoryCatalog)
        collectState(viewModel)

        advanceUntilIdle()

        assertTrue(repositoryCatalog.refreshRequests.isEmpty())
    }

    private fun TestScope.collectState(viewModel: SavedViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
    }

    private class FakeRepositoryCatalog(
        initialSavedRepositories: List<Repository> = emptyList(),
    ) : RepositoryCatalog {
        val savedRepositories = MutableStateFlow(initialSavedRepositories)
        val savedRequests = mutableListOf<Pair<Long, Boolean>>()
        val refreshRequests = mutableListOf<RefreshRequest>()

        override fun observeRepositories(username: String): Flow<List<Repository>> =
            MutableStateFlow(emptyList())

        override fun observeRepository(owner: String, repositoryName: String): Flow<Repository?> =
            MutableStateFlow(null)

        override suspend fun refreshRepositories(
            username: String,
            refreshPolicy: RefreshPolicy,
        ): DataResult<Unit> {
            refreshRequests += RefreshRequest.Repositories(username, refreshPolicy)
            return DataResult.Success(Unit)
        }

        override suspend fun refreshRepository(
            owner: String,
            repositoryName: String,
            refreshPolicy: RefreshPolicy,
        ): DataResult<Unit> {
            refreshRequests += RefreshRequest.Repository(owner, repositoryName, refreshPolicy)
            return DataResult.Success(Unit)
        }

        override suspend fun setRepositorySaved(
            id: Long,
            saved: Boolean,
        ): DataResult<Unit> {
            savedRequests += id to saved
            if (!saved) {
                savedRepositories.update { repositories ->
                    repositories.filterNot { repository -> repository.id == id }
                }
            }
            return DataResult.Success(Unit)
        }

        override fun observeSavedRepositories(): Flow<List<Repository>> =
            savedRepositories
    }

    private sealed interface RefreshRequest {
        data class Repositories(
            val username: String,
            val policy: RefreshPolicy,
        ) : RefreshRequest

        data class Repository(
            val owner: String,
            val repositoryName: String,
            val policy: RefreshPolicy,
        ) : RefreshRequest
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

    private fun sampleRepository(): Repository =
        Repository(
            id = 1296269L,
            ownerUsername = "octocat",
            name = "Hello-World",
            fullName = "octocat/Hello-World",
            description = "My first repository on GitHub.",
            url = "https://github.com/octocat/Hello-World",
            primaryLanguage = "Kotlin",
            starCount = 80,
            forkCount = 9,
            openIssueCount = 3,
            isFork = false,
            isArchived = false,
            isPrivate = false,
            isSaved = true,
            updatedAt = Instant.parse("2026-09-08T09:30:00Z"),
        )
}
