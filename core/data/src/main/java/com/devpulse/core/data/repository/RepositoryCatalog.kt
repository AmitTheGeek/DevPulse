package com.devpulse.core.data.repository

import com.devpulse.core.data.error.DataResult
import com.devpulse.core.model.Repository
import kotlinx.coroutines.flow.Flow

interface RepositoryCatalog {
    fun observeRepositories(username: String): Flow<List<Repository>>

    fun observeRepository(owner: String, repositoryName: String): Flow<Repository?>

    suspend fun refreshRepositories(username: String): DataResult<Unit>

    suspend fun refreshRepository(owner: String, repositoryName: String): DataResult<Unit>

    suspend fun setRepositorySaved(id: Long, saved: Boolean): DataResult<Unit>

    fun observeSavedRepositories(): Flow<List<Repository>>
}
