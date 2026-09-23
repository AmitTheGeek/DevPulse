package com.devpulse.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.devpulse.core.database.entity.RepositoryEntity
import com.devpulse.core.database.model.RepositoryWithSaved
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {
    @Query(
        """
        SELECT repositories.*, saved_repositories.repositoryId IS NOT NULL AS isSaved
        FROM repositories
        LEFT JOIN saved_repositories ON saved_repositories.repositoryId = repositories.id
        WHERE repositories.ownerUsername = :username COLLATE NOCASE
        ORDER BY repositories.starCount DESC, repositories.name COLLATE NOCASE ASC
        """,
    )
    fun observeRepositoriesForDeveloper(username: String): Flow<List<RepositoryWithSaved>>

    @Query(
        """
        SELECT repositories.*, saved_repositories.repositoryId IS NOT NULL AS isSaved
        FROM repositories
        LEFT JOIN saved_repositories ON saved_repositories.repositoryId = repositories.id
        WHERE repositories.ownerUsername = :owner COLLATE NOCASE
        AND repositories.name = :repositoryName COLLATE NOCASE
        LIMIT 1
        """,
    )
    fun observeRepository(owner: String, repositoryName: String): Flow<RepositoryWithSaved?>

    @Query(
        """
        SELECT repositories.*, saved_repositories.repositoryId IS NOT NULL AS isSaved
        FROM repositories
        INNER JOIN saved_repositories ON saved_repositories.repositoryId = repositories.id
        ORDER BY saved_repositories.savedAtEpochMillis DESC
        """,
    )
    fun observeSavedRepositories(): Flow<List<RepositoryWithSaved>>

    @Query("SELECT * FROM repositories WHERE id = :id LIMIT 1")
    suspend fun getRepositoryById(id: Long): RepositoryEntity?

    @Query(
        """
        SELECT * FROM repositories
        WHERE ownerUsername = :owner COLLATE NOCASE
        AND name = :repositoryName COLLATE NOCASE
        LIMIT 1
        """,
    )
    suspend fun getRepository(owner: String, repositoryName: String): RepositoryEntity?

    @Upsert
    suspend fun upsertRepositories(repositories: List<RepositoryEntity>)

    @Upsert
    suspend fun upsertRepository(repository: RepositoryEntity)

    @Query(
        """
        DELETE FROM repositories
        WHERE ownerUsername = :ownerUsername COLLATE NOCASE
        AND id NOT IN (:repositoryIds)
        AND id NOT IN (SELECT repositoryId FROM saved_repositories)
        """,
    )
    suspend fun deleteUnsavedRepositoriesNotIn(ownerUsername: String, repositoryIds: List<Long>)

    @Query(
        """
        UPDATE repositories
        SET ownerListMissingAtEpochMillis = :missingAtEpochMillis
        WHERE ownerUsername = :ownerUsername COLLATE NOCASE
        AND id NOT IN (:repositoryIds)
        AND id IN (SELECT repositoryId FROM saved_repositories)
        """,
    )
    suspend fun markSavedRepositoriesMissingFromOwnerList(
        ownerUsername: String,
        repositoryIds: List<Long>,
        missingAtEpochMillis: Long,
    )

    @Query(
        """
        DELETE FROM repositories
        WHERE ownerUsername = :ownerUsername COLLATE NOCASE
        AND id NOT IN (SELECT repositoryId FROM saved_repositories)
        """,
    )
    suspend fun deleteUnsavedRepositoriesForOwner(ownerUsername: String)

    @Query(
        """
        UPDATE repositories
        SET ownerListMissingAtEpochMillis = :missingAtEpochMillis
        WHERE ownerUsername = :ownerUsername COLLATE NOCASE
        AND id IN (SELECT repositoryId FROM saved_repositories)
        """,
    )
    suspend fun markSavedRepositoriesMissingFromOwnerListForOwner(
        ownerUsername: String,
        missingAtEpochMillis: Long,
    )

    @Query(
        """
        DELETE FROM repositories
        WHERE id = :repositoryId
        AND ownerListMissingAtEpochMillis IS NOT NULL
        AND id NOT IN (SELECT repositoryId FROM saved_repositories)
        """,
    )
    suspend fun deleteRepositoryIfKnownMissingFromOwnerList(repositoryId: Long)
}
