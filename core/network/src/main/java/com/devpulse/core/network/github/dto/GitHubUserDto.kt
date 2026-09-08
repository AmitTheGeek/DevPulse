package com.devpulse.core.network.github.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubUserDto(
    @SerialName("id")
    val id: Long,
    @SerialName("login")
    val login: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("public_repos")
    val publicRepos: Int,
    @SerialName("followers")
    val followers: Int,
    @SerialName("following")
    val following: Int,
)

