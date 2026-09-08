package com.devpulse.core.network.github.service

import com.devpulse.core.network.github.dto.GitHubRepositoryDto
import com.devpulse.core.network.github.dto.GitHubUserDto
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {
    @GET("users/{username}")
    suspend fun getUser(
        @Path("username") username: String,
    ): GitHubUserDto

    @GET("users/{username}/repos")
    suspend fun getUserRepositories(
        @Path("username") username: String,
    ): List<GitHubRepositoryDto>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): GitHubRepositoryDto
}

