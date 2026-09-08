package com.devpulse.core.data.mapper

import com.devpulse.core.database.entity.DeveloperEntity
import com.devpulse.core.database.entity.RepositoryEntity
import com.devpulse.core.model.Developer
import com.devpulse.core.model.Repository
import com.devpulse.core.network.github.dto.GitHubRepositoryDto
import com.devpulse.core.network.github.dto.GitHubUserDto

internal fun GitHubUserDto.toDeveloper(): Developer =
    Developer(
        id = id,
        username = login,
        displayName = name,
        avatarUrl = avatarUrl,
        profileUrl = htmlUrl,
        bio = bio,
        publicRepositoryCount = publicRepos,
        followerCount = followers,
        followingCount = following,
    )

internal fun GitHubUserDto.toDeveloperEntity(): DeveloperEntity =
    DeveloperEntity(
        id = id,
        username = login,
        displayName = name,
        avatarUrl = avatarUrl,
        profileUrl = htmlUrl,
        bio = bio,
        publicRepositoryCount = publicRepos,
        followerCount = followers,
        followingCount = following,
    )

internal fun DeveloperEntity.toDeveloper(): Developer =
    Developer(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        profileUrl = profileUrl,
        bio = bio,
        publicRepositoryCount = publicRepositoryCount,
        followerCount = followerCount,
        followingCount = followingCount,
    )

internal fun GitHubRepositoryDto.toRepository(isSaved: Boolean = false): Repository =
    Repository(
        id = id,
        ownerUsername = owner.login,
        name = name,
        fullName = fullName,
        description = description,
        url = htmlUrl,
        primaryLanguage = language,
        starCount = stargazersCount,
        forkCount = forksCount,
        openIssueCount = openIssuesCount,
        isFork = isFork,
        isArchived = isArchived,
        isPrivate = isPrivate,
        isSaved = isSaved,
        updatedAt = updatedAt,
    )

internal fun GitHubRepositoryDto.toRepositoryEntity(isSaved: Boolean = false): RepositoryEntity =
    RepositoryEntity(
        id = id,
        ownerId = owner.id,
        ownerUsername = owner.login,
        name = name,
        fullName = fullName,
        description = description,
        url = htmlUrl,
        primaryLanguage = language,
        starCount = stargazersCount,
        forkCount = forksCount,
        openIssueCount = openIssuesCount,
        isFork = isFork,
        isArchived = isArchived,
        isPrivate = isPrivate,
        isSaved = isSaved,
        updatedAt = updatedAt,
    )

internal fun RepositoryEntity.toRepository(): Repository =
    Repository(
        id = id,
        ownerUsername = ownerUsername,
        name = name,
        fullName = fullName,
        description = description,
        url = url,
        primaryLanguage = primaryLanguage,
        starCount = starCount,
        forkCount = forkCount,
        openIssueCount = openIssueCount,
        isFork = isFork,
        isArchived = isArchived,
        isPrivate = isPrivate,
        isSaved = isSaved,
        updatedAt = updatedAt,
    )

