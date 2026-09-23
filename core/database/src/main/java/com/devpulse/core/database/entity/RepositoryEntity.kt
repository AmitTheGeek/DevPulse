package com.devpulse.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "repositories",
    indices = [
        Index(value = ["ownerUsername"]),
        Index(value = ["ownerUsername", "name"], unique = true),
        Index(value = ["fullName"], unique = true),
    ],
)
data class RepositoryEntity(
    @PrimaryKey
    val id: Long,
    val ownerId: Long,
    val ownerUsername: String,
    val name: String,
    val fullName: String,
    val description: String?,
    val url: String,
    val primaryLanguage: String?,
    val starCount: Int,
    val forkCount: Int,
    val openIssueCount: Int,
    val isFork: Boolean,
    val isArchived: Boolean,
    val isPrivate: Boolean,
    val updatedAtEpochMillis: Long?,
    val ownerListMissingAtEpochMillis: Long? = null,
)
