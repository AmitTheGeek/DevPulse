package com.devpulse.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.devpulse.core.database.entity.DeveloperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeveloperDao {
    @Query("SELECT * FROM developers WHERE username = :username COLLATE NOCASE LIMIT 1")
    fun observeDeveloper(username: String): Flow<DeveloperEntity?>

    @Query("SELECT * FROM developers WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun getDeveloper(username: String): DeveloperEntity?

    @Upsert
    suspend fun upsertDeveloper(developer: DeveloperEntity)
}
