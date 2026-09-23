package com.devpulse.core.data.repository

import android.database.SQLException
import com.devpulse.core.data.cache.CacheFreshnessChecker
import com.devpulse.core.data.cache.DevPulseClock
import com.devpulse.core.data.cache.SyncKeys
import com.devpulse.core.data.error.DataResult
import com.devpulse.core.data.error.toDevPulseError
import com.devpulse.core.data.mapper.toDeveloper
import com.devpulse.core.data.sync.syncDeveloperSnapshot
import com.devpulse.core.database.DevPulseDatabase
import com.devpulse.core.model.Developer
import com.devpulse.core.network.github.service.GitHubApi
import com.devpulse.core.network.github.service.GitHubApiPaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DefaultDeveloperRepository @Inject constructor(
    private val database: DevPulseDatabase,
    private val gitHubApi: GitHubApi,
    private val clock: DevPulseClock,
    private val cacheFreshnessChecker: CacheFreshnessChecker,
) : DeveloperRepository {
    override fun observeDeveloper(username: String): Flow<Developer?> =
        database.developerDao()
            .observeDeveloper(username)
            .map { it?.toDeveloper() }

    override suspend fun refreshDeveloper(
        username: String,
        refreshPolicy: RefreshPolicy,
    ): DataResult<Unit> {
        return try {
            if (
                refreshPolicy == RefreshPolicy.IfStale &&
                cacheFreshnessChecker.isFresh(SyncKeys.developer(username)) &&
                cacheFreshnessChecker.isFresh(SyncKeys.repositories(username))
            ) {
                DataResult.Success(Unit)
            } else {
                val developer = gitHubApi.getUser(username)
                val repositories = gitHubApi.getUserRepositories(
                    username = username,
                    perPage = GitHubApiPaging.MAX_PAGE_SIZE,
                    page = GitHubApiPaging.FIRST_PAGE,
                )

                database.syncDeveloperSnapshot(
                    developer = developer,
                    repositories = repositories,
                    refreshedAtEpochMillis = clock.nowEpochMillis(),
                )

                DataResult.Success(Unit)
            }
        } catch (exception: IOException) {
            DataResult.Failure(exception.toDevPulseError())
        } catch (exception: HttpException) {
            DataResult.Failure(exception.toDevPulseError())
        } catch (exception: SQLException) {
            DataResult.Failure(exception.toDevPulseError())
        }
    }
}
