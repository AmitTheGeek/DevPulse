package com.devpulse.core.data.mapper

import com.devpulse.core.database.entity.DeveloperEntity
import com.devpulse.core.database.entity.RepositoryEntity
import com.devpulse.core.network.github.dto.GitHubRepositoryDto
import com.devpulse.core.network.github.dto.GitHubRepositoryOwnerDto
import com.devpulse.core.network.github.dto.GitHubUserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubMappersTest {
    @Test
    fun userDtoMapsToDeveloper() {
        val dto = sampleUserDto()

        val developer = dto.toDeveloper()

        assertEquals(42L, developer.id)
        assertEquals("octocat", developer.username)
        assertEquals("The Octocat", developer.displayName)
        assertEquals("https://avatars.githubusercontent.com/u/42?v=4", developer.avatarUrl)
        assertEquals("https://github.com/octocat", developer.profileUrl)
        assertEquals("GitHub mascot", developer.bio)
        assertEquals(8, developer.publicRepositoryCount)
        assertEquals(9001, developer.followerCount)
        assertEquals(12, developer.followingCount)
    }

    @Test
    fun userDtoMapsToDeveloperEntity() {
        val dto = sampleUserDto()

        val entity = dto.toDeveloperEntity()

        assertEquals(42L, entity.id)
        assertEquals("octocat", entity.username)
        assertEquals("The Octocat", entity.displayName)
        assertEquals("https://avatars.githubusercontent.com/u/42?v=4", entity.avatarUrl)
        assertEquals("https://github.com/octocat", entity.profileUrl)
        assertEquals("GitHub mascot", entity.bio)
        assertEquals(8, entity.publicRepositoryCount)
        assertEquals(9001, entity.followerCount)
        assertEquals(12, entity.followingCount)
    }

    @Test
    fun developerEntityMapsToDeveloper() {
        val entity = DeveloperEntity(
            id = 42L,
            username = "octocat",
            displayName = "The Octocat",
            avatarUrl = "https://avatars.githubusercontent.com/u/42?v=4",
            profileUrl = "https://github.com/octocat",
            bio = "GitHub mascot",
            publicRepositoryCount = 8,
            followerCount = 9001,
            followingCount = 12,
        )

        val developer = entity.toDeveloper()

        assertEquals(entity.id, developer.id)
        assertEquals(entity.username, developer.username)
        assertEquals(entity.displayName, developer.displayName)
        assertEquals(entity.avatarUrl, developer.avatarUrl)
        assertEquals(entity.profileUrl, developer.profileUrl)
        assertEquals(entity.bio, developer.bio)
        assertEquals(entity.publicRepositoryCount, developer.publicRepositoryCount)
        assertEquals(entity.followerCount, developer.followerCount)
        assertEquals(entity.followingCount, developer.followingCount)
    }

    @Test
    fun repositoryDtoMapsToRepository() {
        val dto = sampleRepositoryDto()

        val repository = dto.toRepository(isSaved = true)

        assertEquals(1296269L, repository.id)
        assertEquals("octocat", repository.ownerUsername)
        assertEquals("Hello-World", repository.name)
        assertEquals("octocat/Hello-World", repository.fullName)
        assertEquals("My first repository on GitHub.", repository.description)
        assertEquals("https://github.com/octocat/Hello-World", repository.url)
        assertEquals("Kotlin", repository.primaryLanguage)
        assertEquals(80, repository.starCount)
        assertEquals(9, repository.forkCount)
        assertEquals(3, repository.openIssueCount)
        assertFalse(repository.isFork)
        assertFalse(repository.isArchived)
        assertFalse(repository.isPrivate)
        assertTrue(repository.isSaved)
        assertEquals("2026-09-08T09:30:00Z", repository.updatedAt)
    }

    @Test
    fun repositoryDtoMapsToRepositoryEntity() {
        val dto = sampleRepositoryDto()

        val entity = dto.toRepositoryEntity(isSaved = true)

        assertEquals(1296269L, entity.id)
        assertEquals(42L, entity.ownerId)
        assertEquals("octocat", entity.ownerUsername)
        assertEquals("Hello-World", entity.name)
        assertEquals("octocat/Hello-World", entity.fullName)
        assertEquals("My first repository on GitHub.", entity.description)
        assertEquals("https://github.com/octocat/Hello-World", entity.url)
        assertEquals("Kotlin", entity.primaryLanguage)
        assertEquals(80, entity.starCount)
        assertEquals(9, entity.forkCount)
        assertEquals(3, entity.openIssueCount)
        assertFalse(entity.isFork)
        assertFalse(entity.isArchived)
        assertFalse(entity.isPrivate)
        assertTrue(entity.isSaved)
        assertEquals("2026-09-08T09:30:00Z", entity.updatedAt)
    }

    @Test
    fun repositoryEntityMapsToRepository() {
        val entity = RepositoryEntity(
            id = 1296269L,
            ownerId = 42L,
            ownerUsername = "octocat",
            name = "Hello-World",
            fullName = "octocat/Hello-World",
            description = "My first repository on GitHub.",
            url = "https://github.com/octocat/Hello-World",
            primaryLanguage = "Kotlin",
            starCount = 80,
            forkCount = 9,
            openIssueCount = 3,
            isFork = false,
            isArchived = false,
            isPrivate = false,
            isSaved = true,
            updatedAt = "2026-09-08T09:30:00Z",
        )

        val repository = entity.toRepository()

        assertEquals(entity.id, repository.id)
        assertEquals(entity.ownerUsername, repository.ownerUsername)
        assertEquals(entity.name, repository.name)
        assertEquals(entity.fullName, repository.fullName)
        assertEquals(entity.description, repository.description)
        assertEquals(entity.url, repository.url)
        assertEquals(entity.primaryLanguage, repository.primaryLanguage)
        assertEquals(entity.starCount, repository.starCount)
        assertEquals(entity.forkCount, repository.forkCount)
        assertEquals(entity.openIssueCount, repository.openIssueCount)
        assertEquals(entity.isFork, repository.isFork)
        assertEquals(entity.isArchived, repository.isArchived)
        assertEquals(entity.isPrivate, repository.isPrivate)
        assertEquals(entity.isSaved, repository.isSaved)
        assertEquals(entity.updatedAt, repository.updatedAt)
    }

    private fun sampleUserDto(): GitHubUserDto =
        GitHubUserDto(
            id = 42L,
            login = "octocat",
            name = "The Octocat",
            avatarUrl = "https://avatars.githubusercontent.com/u/42?v=4",
            htmlUrl = "https://github.com/octocat",
            bio = "GitHub mascot",
            publicRepos = 8,
            followers = 9001,
            following = 12,
        )

    private fun sampleRepositoryDto(): GitHubRepositoryDto =
        GitHubRepositoryDto(
            id = 1296269L,
            name = "Hello-World",
            fullName = "octocat/Hello-World",
            owner = GitHubRepositoryOwnerDto(
                id = 42L,
                login = "octocat",
                avatarUrl = "https://avatars.githubusercontent.com/u/42?v=4",
                htmlUrl = "https://github.com/octocat",
            ),
            description = "My first repository on GitHub.",
            htmlUrl = "https://github.com/octocat/Hello-World",
            language = "Kotlin",
            stargazersCount = 80,
            forksCount = 9,
            openIssuesCount = 3,
            isFork = false,
            isArchived = false,
            isPrivate = false,
            updatedAt = "2026-09-08T09:30:00Z",
        )
}
