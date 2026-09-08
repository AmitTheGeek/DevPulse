package com.devpulse.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_repositories")
data class SavedRepositoryEntity(
    @PrimaryKey
    val repositoryId: Long,
    val savedAtEpochMillis: Long,
)
