package com.devpulse.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.devpulse.core.database.entity.SavedRepositoryEntity

@Dao
interface SavedRepositoryDao {
    @Query("SELECT * FROM saved_repositories WHERE repositoryId = :repositoryId LIMIT 1")
    suspend fun getSavedRepository(repositoryId: Long): SavedRepositoryEntity?

    @Upsert
    suspend fun upsertSavedRepository(savedRepository: SavedRepositoryEntity)

    @Query("DELETE FROM saved_repositories WHERE repositoryId = :repositoryId")
    suspend fun deleteSavedRepository(repositoryId: Long)
}

