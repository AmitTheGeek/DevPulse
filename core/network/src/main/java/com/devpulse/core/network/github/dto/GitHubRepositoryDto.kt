package com.devpulse.core.network.github.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryDto(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("owner")
    val owner: GitHubRepositoryOwnerDto,
    @SerialName("description")
    val description: String? = null,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("language")
    val language: String? = null,
    @SerialName("stargazers_count")
    val stargazersCount: Int,
    @SerialName("forks_count")
    val forksCount: Int,
    @SerialName("open_issues_count")
    val openIssuesCount: Int,
    @SerialName("fork")
    val isFork: Boolean,
    @SerialName("archived")
    val isArchived: Boolean,
    @SerialName("private")
    val isPrivate: Boolean,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)

