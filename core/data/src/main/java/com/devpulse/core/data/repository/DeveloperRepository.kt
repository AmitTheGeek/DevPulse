package com.devpulse.core.data.repository

import com.devpulse.core.data.error.DataResult
import com.devpulse.core.model.Developer
import kotlinx.coroutines.flow.Flow

interface DeveloperRepository {
    fun observeDeveloper(username: String): Flow<Developer?>

    suspend fun refreshDeveloper(
        username: String,
        refreshPolicy: RefreshPolicy = RefreshPolicy.Force,
    ): DataResult<Unit>
}
