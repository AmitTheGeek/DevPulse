package com.devpulse.core.database.model

import androidx.room.ColumnInfo

data class RepositoryWithSaved(
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
    @ColumnInfo(name = "isSaved")
    val isSaved: Boolean,
)

