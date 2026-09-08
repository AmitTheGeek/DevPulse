package com.devpulse.core.network.github.service

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object GitHubNetworkConfig {
    const val BASE_URL = "https://api.github.com/"
}

internal val GitHubJson = Json {
    ignoreUnknownKeys = true
}

object GitHubApiFactory {
    fun create(
        okHttpClient: OkHttpClient = OkHttpClient(),
        baseUrl: String = GitHubNetworkConfig.BASE_URL,
    ): GitHubApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GitHubJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GitHubApi::class.java)
}
