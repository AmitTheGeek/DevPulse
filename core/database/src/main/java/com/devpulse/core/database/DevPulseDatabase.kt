package com.devpulse.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.devpulse.core.database.dao.DeveloperDao
import com.devpulse.core.database.dao.RepositoryDao
import com.devpulse.core.database.dao.SavedRepositoryDao
import com.devpulse.core.database.dao.SyncMetadataDao
import com.devpulse.core.database.entity.DeveloperEntity
import com.devpulse.core.database.entity.RepositoryEntity
import com.devpulse.core.database.entity.SavedRepositoryEntity
import com.devpulse.core.database.entity.SyncMetadataEntity

@Database(
    entities = [
        DeveloperEntity::class,
        RepositoryEntity::class,
        SavedRepositoryEntity::class,
        SyncMetadataEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class DevPulseDatabase : RoomDatabase() {
    abstract fun developerDao(): DeveloperDao
    abstract fun repositoryDao(): RepositoryDao
    abstract fun savedRepositoryDao(): SavedRepositoryDao
    abstract fun syncMetadataDao(): SyncMetadataDao
}
