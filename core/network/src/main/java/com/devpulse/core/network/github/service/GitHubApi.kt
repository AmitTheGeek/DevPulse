package com.devpulse.core.network.github.service

import com.devpulse.core.network.github.dto.GitHubRepositoryDto
import com.devpulse.core.network.github.dto.GitHubUserDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {
    @GET("users/{username}")
    suspend fun getUser(
        @Path("username") username: String,
    ): GitHubUserDto

    @GET("users/{username}/repos")
    suspend fun getUserRepositories(
        @Path("username") username: String,
        @Query("per_page") perPage: Int = GitHubApiPaging.MAX_PAGE_SIZE,
        @Query("page") page: Int = GitHubApiPaging.FIRST_PAGE,
    ): List<GitHubRepositoryDto>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): GitHubRepositoryDto
}

object GitHubApiPaging {
    const val FIRST_PAGE = 1
    const val MAX_PAGE_SIZE = 100
}
