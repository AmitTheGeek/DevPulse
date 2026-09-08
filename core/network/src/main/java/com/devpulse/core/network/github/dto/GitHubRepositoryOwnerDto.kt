package com.devpulse.core.network.github.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryOwnerDto(
    @SerialName("id")
    val id: Long,
    @SerialName("login")
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("html_url")
    val htmlUrl: String? = null,
)

