package com.devpulse.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.devpulse.core.data.cache.CacheFreshnessChecker
import com.devpulse.core.data.cache.CacheFreshnessPolicy
import com.devpulse.core.data.cache.DevPulseClock
import com.devpulse.core.data.cache.SyncKeys
import com.devpulse.core.data.error.DataResult
import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.database.DevPulseDatabase
import com.devpulse.core.database.entity.RepositoryEntity
import com.devpulse.core.database.entity.SyncMetadataEntity
import com.devpulse.core.network.github.dto.GitHubRepositoryDto
import com.devpulse.core.network.github.dto.GitHubRepositoryOwnerDto
import com.devpulse.core.network.github.dto.GitHubUserDto
import com.devpulse.core.network.github.service.GitHubApi
import com.devpulse.core.network.github.service.GitHubApiPaging
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class OfflineFirstRepositoryTest {
    private lateinit var database: DevPulseDatabase
    private lateinit var gitHubApi: FakeGitHubApi
    private lateinit var clock: FixedClock
    private lateinit var developerRepository: DefaultDeveloperRepository
    private lateinit var repositoryCatalog: DefaultRepositoryCatalog

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DevPulseDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        gitHubApi = FakeGitHubApi()
        clock = FixedClock(nowEpochMillis = 10_000L)
        developerRepository = DefaultDeveloperRepository(
            database = database,
            gitHubApi = gitHubApi,
            clock = clock,
        )
        repositoryCatalog = DefaultRepositoryCatalog(
            database = database,
            gitHubApi = gitHubApi,
            clock = clock,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun repositoryObservationMapsRoomEntitiesToDomainModels() = runTest {
        database.repositoryDao().upsertRepository(sampleRepositoryEntity())

        val repositories = repositoryCatalog.observeRepositories("OCTOCAT").first()

        assertEquals(1, repositories.size)
        val repository = repositories.single()
        assertEquals(1296269L, repository.id)
        assertEquals("octocat", repository.ownerUsername)
        assertEquals("Hello-World", repository.name)
        assertEquals("octocat/Hello-World", repository.fullName)
        assertEquals("https://github.com/octocat/Hello-World", repository.url)
        assertEquals("Kotlin", repository.primaryLanguage)
        assertEquals(80, repository.starCount)
        assertFalse(repository.isSaved)
        assertEquals(Instant.parse("2026-09-08T09:30:00Z"), repository.updatedAt)
    }

    @Test
    fun successfulRepositoryRefreshWritesRemoteResultsToRoom() = runTest {
        gitHubApi.repositoriesResult = Result.success(
            listOf(sampleRepositoryDto(id = 1L, name = "Pulse")),
        )

        val result = repositoryCatalog.refreshRepositories("octocat")

        assertSuccess(result)
        val storedRepository = database.repositoryDao().getRepository("octocat", "Pulse")
        assertNotNull(storedRepository)
        assertEquals(1L, storedRepository?.id)
        assertEquals(80, storedRepository?.starCount)
        assertEquals(
            FakeGitHubApi.RepositoryListRequest(
                username = "octocat",
                perPage = GitHubApiPaging.MAX_PAGE_SIZE,
                page = GitHubApiPaging.FIRST_PAGE,
            ),
            gitHubApi.repositoryListRequests.single(),
        )
    }

    @Test
    fun flowObserversReceiveRefreshedRepositoryData() = runTest {
        gitHubApi.repositoriesResult = Result.success(
            listOf(sampleRepositoryDto(id = 2L, name = "Flow-Repo")),
        )

        val observedRepositories = async {
            repositoryCatalog.observeRepositories("octocat")
                .filter { repositories -> repositories.any { it.name == "Flow-Repo" } }
                .first()
        }

        assertSuccess(repositoryCatalog.refreshRepositories("octocat"))

        assertEquals("Flow-Repo", observedRepositories.await().single().name)
    }

    @Test
    fun refreshFailurePreservesExistingCachedRepositoryData() = runTest {
        database.repositoryDao().upsertRepository(sampleRepositoryEntity(id = 3L, name = "Cached"))
        gitHubApi.repositoriesResult = Result.failure(IOException("offline"))

        val result = repositoryCatalog.refreshRepositories("octocat")

        assertFailure<DevPulseError.NetworkUnavailable>(result)
        val repositories = repositoryCatalog.observeRepositories("octocat").first()
        assertEquals("Cached", repositories.single().name)
    }

    @Test
    fun completeListRefreshRemovesUnsavedRepositoriesMissingFromRemote() = runTest {
        database.repositoryDao().upsertRepositories(
            listOf(
                sampleRepositoryEntity(id = 4L, name = "Still-Remote"),
                sampleRepositoryEntity(id = 5L, name = "Removed-Remote"),
            ),
        )
        gitHubApi.repositoriesResult = Result.success(
            listOf(sampleRepositoryDto(id = 4L, name = "Still-Remote")),
        )

        assertSuccess(repositoryCatalog.refreshRepositories("octocat"))

        val repositories = repositoryCatalog.observeRepositories("octocat").first()
        assertEquals(listOf("Still-Remote"), repositories.map { it.name })
    }

    @Test
    fun fullPageRefreshDefersMissingRepositoryDeletionUntilPaginationExists() = runTest {
        database.repositoryDao().upsertRepository(sampleRepositoryEntity(id = 6L, name = "Maybe-Next-Page"))
        gitHubApi.repositoriesResult = Result.success(
            (1L..GitHubApiPaging.MAX_PAGE_SIZE).map { index ->
                sampleRepositoryDto(id = 1_000L + index, name = "Remote-$index")
            },
        )

        assertSuccess(repositoryCatalog.refreshRepositories("octocat"))

        val repositories = repositoryCatalog.observeRepositories("octocat").first()
        assertTrue(repositories.any { it.name == "Maybe-Next-Page" })
    }

    @Test
    fun savedStateSurvivesRepositoryRefresh() = runTest {
        database.repositoryDao().upsertRepository(sampleRepositoryEntity(id = 106L, name = "Saved"))
        assertSuccess(repositoryCatalog.setRepositorySaved(id = 106L, saved = true))
        gitHubApi.repositoryResult = Result.success(
            sampleRepositoryDto(id = 106L, name = "Saved", starCount = 120),
        )

        assertSuccess(repositoryCatalog.refreshRepository("octocat", "Saved"))

        val repository = repositoryCatalog.observeRepository("octocat", "Saved").first()
        assertNotNull(repository)
        assertTrue(repository?.isSaved == true)
        assertEquals(120, repository?.starCount)
    }

    @Test
    fun savedRepositoriesObservationReturnsLocallySavedRepositories() = runTest {
        database.repositoryDao().upsertRepositories(
            listOf(
                sampleRepositoryEntity(id = 7L, name = "Saved-One"),
                sampleRepositoryEntity(id = 8L, name = "Not-Saved"),
            ),
        )
        assertSuccess(repositoryCatalog.setRepositorySaved(id = 7L, saved = true))

        val savedRepositories = repositoryCatalog.observeSavedRepositories().first()

        assertEquals(1, savedRepositories.size)
        assertEquals("Saved-One", savedRepositories.single().name)
        assertTrue(savedRepositories.single().isSaved)
    }

    @Test
    fun developerRefreshSynchronizesProfileAndRepositoryList() = runTest {
        gitHubApi.userResult = Result.success(sampleUserDto(username = "octocat"))
        gitHubApi.repositoriesResult = Result.success(
            listOf(sampleRepositoryDto(id = 9L, name = "Developer-Repo")),
        )

        val result = developerRepository.refreshDeveloper("Octocat")

        assertSuccess(result)
        assertEquals("octocat", developerRepository.observeDeveloper("OCTOCAT").first()?.username)
        assertEquals(
            listOf("Developer-Repo"),
            repositoryCatalog.observeRepositories("octocat").first().map { it.name },
        )
    }

    @Test
    fun rateLimitErrorsMapToRateLimitedDataError() = runTest {
        gitHubApi.repositoriesResult = Result.failure(rateLimitException())

        val result = repositoryCatalog.refreshRepositories("octocat")

        assertTrue(result is DataResult.Failure)
        val error = (result as DataResult.Failure).error
        assertTrue(error is DevPulseError.RateLimited)
        error as DevPulseError.RateLimited
        assertEquals(403, error.statusCode)
        assertEquals(1_788_859_800L, error.resetEpochSeconds)
    }

    @Test
    fun cacheFreshnessPolicyUsesStoredMetadataAndInjectedClock() = runTest {
        val policy = CacheFreshnessPolicy(ttlMillis = 1_000L)
        val checker = CacheFreshnessChecker(
            syncMetadataDao = database.syncMetadataDao(),
            clock = clock,
            cacheFreshnessPolicy = policy,
        )
        val syncKey = SyncKeys.repositories("octocat")

        assertFalse(checker.isFresh(syncKey))

        database.syncMetadataDao().upsertSyncMetadata(
            SyncMetadataEntity(
                syncKey = syncKey,
                refreshedAtEpochMillis = 9_500L,
            ),
        )

        assertTrue(checker.isFresh(syncKey))

        clock.nowEpochMillis = 10_501L

        assertFalse(checker.isFresh(syncKey))
    }

    private fun sampleUserDto(username: String = "octocat"): GitHubUserDto =
        GitHubUserDto(
            id = 42L,
            login = username,
            name = "The Octocat",
            avatarUrl = "https://avatars.githubusercontent.com/u/42?v=4",
            htmlUrl = "https://github.com/$username",
            bio = "GitHub mascot",
            publicRepos = 8,
            followers = 9001,
            following = 12,
        )

    private fun sampleRepositoryDto(
        id: Long = 1296269L,
        name: String = "Hello-World",
        owner: String = "octocat",
        ownerId: Long = 42L,
        starCount: Int = 80,
    ): GitHubRepositoryDto =
        GitHubRepositoryDto(
            id = id,
            name = name,
            fullName = "$owner/$name",
            owner = GitHubRepositoryOwnerDto(
                id = ownerId,
                login = owner,
                avatarUrl = "https://avatars.githubusercontent.com/u/$ownerId?v=4",
                htmlUrl = "https://github.com/$owner",
            ),
            description = "My first repository on GitHub.",
            htmlUrl = "https://github.com/$owner/$name",
            language = "Kotlin",
            stargazersCount = starCount,
            forksCount = 9,
            openIssuesCount = 3,
            isFork = false,
            isArchived = false,
            isPrivate = false,
            updatedAt = "2026-09-08T09:30:00Z",
        )

    private fun sampleRepositoryEntity(
        id: Long = 1296269L,
        name: String = "Hello-World",
        owner: String = "octocat",
    ): RepositoryEntity =
        RepositoryEntity(
            id = id,
            ownerId = 42L,
            ownerUsername = owner,
            name = name,
            fullName = "$owner/$name",
            description = "My first repository on GitHub.",
            url = "https://github.com/$owner/$name",
            primaryLanguage = "Kotlin",
            starCount = 80,
            forkCount = 9,
            openIssueCount = 3,
            isFork = false,
            isArchived = false,
            isPrivate = false,
            updatedAtEpochMillis = 1_788_859_800_000L,
        )

    private fun rateLimitException(): HttpException {
        val body = """{"message":"API rate limit exceeded"}"""
            .toResponseBody("application/json".toMediaType())
        val rawResponse = okhttp3.Response.Builder()
            .request(
                Request.Builder()
                    .url("https://api.github.com/users/octocat/repos")
                    .build(),
            )
            .protocol(Protocol.HTTP_1_1)
            .code(403)
            .message("rate limit")
            .headers(
                Headers.Builder()
                    .add("X-RateLimit-Remaining", "0")
                    .add("X-RateLimit-Reset", "1788859800")
                    .build(),
            )
            .build()

        return HttpException(Response.error<Any>(body, rawResponse))
    }

    private fun assertSuccess(result: DataResult<Unit>) {
        assertTrue(result is DataResult.Success)
    }

    private inline fun <reified T : DevPulseError> assertFailure(result: DataResult<Unit>) {
        assertTrue(result is DataResult.Failure)
        assertTrue((result as DataResult.Failure).error is T)
    }

    private class FixedClock(
        var nowEpochMillis: Long,
    ) : DevPulseClock {
        override fun nowEpochMillis(): Long = nowEpochMillis
    }

    private class FakeGitHubApi : GitHubApi {
        var userResult: Result<GitHubUserDto> = Result.success(
            GitHubUserDto(
                id = 42L,
                login = "octocat",
                name = "The Octocat",
                avatarUrl = "https://avatars.githubusercontent.com/u/42?v=4",
                htmlUrl = "https://github.com/octocat",
                bio = "GitHub mascot",
                publicRepos = 8,
                followers = 9001,
                following = 12,
            ),
        )
        var repositoriesResult: Result<List<GitHubRepositoryDto>> = Result.success(emptyList())
        var repositoryResult: Result<GitHubRepositoryDto> = Result.failure(
            AssertionError("repository result was not configured"),
        )
        val repositoryListRequests = mutableListOf<RepositoryListRequest>()

        override suspend fun getUser(username: String): GitHubUserDto =
            userResult.getOrThrow()

        override suspend fun getUserRepositories(
            username: String,
            perPage: Int,
            page: Int,
        ): List<GitHubRepositoryDto> {
            repositoryListRequests += RepositoryListRequest(
                username = username,
                perPage = perPage,
                page = page,
            )
            return repositoriesResult.getOrThrow()
        }

        override suspend fun getRepository(owner: String, repo: String): GitHubRepositoryDto =
            repositoryResult.getOrThrow()

        data class RepositoryListRequest(
            val username: String,
            val perPage: Int,
            val page: Int,
        )
    }
}
