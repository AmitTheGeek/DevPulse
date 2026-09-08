package com.devpulse.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "developers",
    indices = [
        Index(value = ["username"], unique = true),
    ],
)
data class DeveloperEntity(
    @PrimaryKey
    val id: Long,
    val username: String,
    val displayName: String?,
    val avatarUrl: String,
    val profileUrl: String,
    val bio: String?,
    val publicRepositoryCount: Int,
    val followerCount: Int,
    val followingCount: Int,
)

