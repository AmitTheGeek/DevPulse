package com.devpulse.core.data.sync

import androidx.room.withTransaction
import com.devpulse.core.data.cache.SyncKeys
import com.devpulse.core.data.mapper.toDeveloperEntity
import com.devpulse.core.data.mapper.toRepositoryEntity
import com.devpulse.core.database.DevPulseDatabase
import com.devpulse.core.database.entity.SyncMetadataEntity
import com.devpulse.core.network.github.dto.GitHubRepositoryDto
import com.devpulse.core.network.github.dto.GitHubUserDto
import com.devpulse.core.network.github.service.GitHubApiPaging

internal suspend fun DevPulseDatabase.syncDeveloperSnapshot(
    developer: GitHubUserDto,
    repositories: List<GitHubRepositoryDto>,
    refreshedAtEpochMillis: Long,
) {
    withTransaction {
        developerDao().upsertDeveloper(developer.toDeveloperEntity())
        syncRepositoryList(
            ownerUsername = repositories.firstOrNull()?.owner?.login ?: developer.login,
            repositories = repositories,
            isCompleteSnapshot = repositories.isKnownCompletePage(),
        )
        syncMetadataDao().upsertSyncMetadata(
            SyncMetadataEntity(
                syncKey = SyncKeys.developer(developer.login),
                refreshedAtEpochMillis = refreshedAtEpochMillis,
            ),
        )
        syncMetadataDao().upsertSyncMetadata(
            SyncMetadataEntity(
                syncKey = SyncKeys.repositories(developer.login),
                refreshedAtEpochMillis = refreshedAtEpochMillis,
            ),
        )
    }
}

internal suspend fun DevPulseDatabase.syncRepositoryListSnapshot(
    requestedUsername: String,
    repositories: List<GitHubRepositoryDto>,
    refreshedAtEpochMillis: Long,
) {
    val ownerUsername = repositories.firstOrNull()?.owner?.login ?: requestedUsername

    withTransaction {
        syncRepositoryList(
            ownerUsername = ownerUsername,
            repositories = repositories,
            isCompleteSnapshot = repositories.isKnownCompletePage(),
        )
        syncMetadataDao().upsertSyncMetadata(
            SyncMetadataEntity(
                syncKey = SyncKeys.repositories(ownerUsername),
                refreshedAtEpochMillis = refreshedAtEpochMillis,
            ),
        )
    }
}

internal suspend fun DevPulseDatabase.syncRepositorySnapshot(
    repository: GitHubRepositoryDto,
    refreshedAtEpochMillis: Long,
) {
    withTransaction {
        repositoryDao().upsertRepository(repository.toRepositoryEntity())
        syncMetadataDao().upsertSyncMetadata(
            SyncMetadataEntity(
                syncKey = SyncKeys.repository(
                    owner = repository.owner.login,
                    repositoryName = repository.name,
                ),
                refreshedAtEpochMillis = refreshedAtEpochMillis,
            ),
        )
    }
}

private suspend fun DevPulseDatabase.syncRepositoryList(
    ownerUsername: String,
    repositories: List<GitHubRepositoryDto>,
    isCompleteSnapshot: Boolean,
) {
    repositoryDao().upsertRepositories(repositories.map { it.toRepositoryEntity() })
    if (!isCompleteSnapshot) return

    val repositoryIds = repositories.map { it.id }
    if (repositoryIds.isEmpty()) {
        repositoryDao().deleteUnsavedRepositoriesForOwner(ownerUsername)
    } else {
        repositoryDao().deleteUnsavedRepositoriesNotIn(
            ownerUsername = ownerUsername,
            repositoryIds = repositoryIds,
        )
    }
}

private fun List<GitHubRepositoryDto>.isKnownCompletePage(): Boolean =
    size < GitHubApiPaging.MAX_PAGE_SIZE
