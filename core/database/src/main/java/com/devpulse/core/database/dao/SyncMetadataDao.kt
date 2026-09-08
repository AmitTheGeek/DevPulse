package com.devpulse.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.devpulse.core.database.entity.SyncMetadataEntity

@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM sync_metadata WHERE syncKey = :syncKey LIMIT 1")
    suspend fun getSyncMetadata(syncKey: String): SyncMetadataEntity?

    @Upsert
    suspend fun upsertSyncMetadata(syncMetadata: SyncMetadataEntity)
}

