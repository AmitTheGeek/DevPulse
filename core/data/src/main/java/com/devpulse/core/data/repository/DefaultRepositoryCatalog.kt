package com.devpulse.core.data.repository

import androidx.room.withTransaction
import com.devpulse.core.data.cache.CacheFreshnessChecker
import com.devpulse.core.data.cache.DevPulseClock
import com.devpulse.core.data.cache.SyncKeys
import com.devpulse.core.data.error.DataResult
import com.devpulse.core.data.error.DevPulseError
import com.devpulse.core.data.error.dataResultOf
import com.devpulse.core.data.error.toDevPulseError
import com.devpulse.core.data.mapper.toRepository
import com.devpulse.core.data.sync.syncRepositoryListSnapshot
import com.devpulse.core.data.sync.syncRepositorySnapshot
import com.devpulse.core.database.DevPulseDatabase
import com.devpulse.core.database.entity.SavedRepositoryEntity
import com.devpulse.core.model.Repository
import com.devpulse.core.network.github.service.GitHubApi
import com.devpulse.core.network.github.service.GitHubApiPaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultRepositoryCatalog @Inject constructor(
    private val database: DevPulseDatabase,
    private val gitHubApi: GitHubApi,
    private val clock: DevPulseClock,
    private val cacheFreshnessChecker: CacheFreshnessChecker,
) : RepositoryCatalog {
    override fun observeRepositories(username: String): Flow<List<Repository>> =
        database.repositoryDao()
            .observeRepositoriesForDeveloper(username)
            .map { repositories -> repositories.map { it.toRepository() } }

    override fun observeRepository(owner: String, repositoryName: String): Flow<Repository?> =
        database.repositoryDao()
            .observeRepository(owner, repositoryName)
            .map { it?.toRepository() }

    override suspend fun refreshRepositories(
        username: String,
        refreshPolicy: RefreshPolicy,
    ): DataResult<Unit> =
        dataResultOf {
            val isFresh =
                refreshPolicy == RefreshPolicy.IfStale &&
                cacheFreshnessChecker.isFresh(SyncKeys.repositories(username))

            if (!isFresh) {
                val repositories = gitHubApi.getUserRepositories(
                    username = username,
                    perPage = GitHubApiPaging.MAX_PAGE_SIZE,
                    page = GitHubApiPaging.FIRST_PAGE,
                )

                database.syncRepositoryListSnapshot(
                    requestedUsername = username,
                    repositories = repositories,
                    refreshedAtEpochMillis = clock.nowEpochMillis(),
                )
            }
        }

    override suspend fun refreshRepository(
        owner: String,
        repositoryName: String,
        refreshPolicy: RefreshPolicy,
    ): DataResult<Unit> =
        dataResultOf {
            val isFresh =
                refreshPolicy == RefreshPolicy.IfStale &&
                cacheFreshnessChecker.isFresh(SyncKeys.repository(owner, repositoryName))

            if (!isFresh) {
                val repository = gitHubApi.getRepository(
                    owner = owner,
                    repo = repositoryName,
                )

                database.syncRepositorySnapshot(
                    repository = repository,
                    refreshedAtEpochMillis = clock.nowEpochMillis(),
                )
            }
        }

    override suspend fun setRepositorySaved(id: Long, saved: Boolean): DataResult<Unit> =
        dataResultOf(
            mapException = { exception ->
                if (exception is RepositoryNotFoundException) {
                    DevPulseError.NotFound
                } else {
                    exception.toDevPulseError()
                }
            },
        ) {
            database.withTransaction {
                if (saved) {
                    val repository = database.repositoryDao().getRepositoryById(id)
                        ?: throw RepositoryNotFoundException
                    database.savedRepositoryDao().upsertSavedRepository(
                        SavedRepositoryEntity(
                            repositoryId = repository.id,
                            savedAtEpochMillis = clock.nowEpochMillis(),
                        ),
                    )
                } else {
                    database.savedRepositoryDao().deleteSavedRepository(id)
                    database.repositoryDao().deleteRepositoryIfKnownMissingFromOwnerList(id)
                }
            }
        }

    override fun observeSavedRepositories(): Flow<List<Repository>> =
        database.repositoryDao()
            .observeSavedRepositories()
            .map { repositories -> repositories.map { it.toRepository() } }
}

private object RepositoryNotFoundException : Exception()
