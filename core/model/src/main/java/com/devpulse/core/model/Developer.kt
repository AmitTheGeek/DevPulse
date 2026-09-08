package com.devpulse.core.model

data class Developer(
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

