package com.devpulse.core.model

import java.time.Instant

data class Repository(
    val id: Long,
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
    val isSaved: Boolean,
    val updatedAt: Instant?,
)
