package com.devpulse.feature.repository

import android.net.Uri

object RepositoryDestination {
    const val ROUTE_BASE = "repository"
    const val OWNER_ARGUMENT = "owner"
    const val REPOSITORY_NAME_ARGUMENT = "repositoryName"
    const val ROUTE_PATTERN = "$ROUTE_BASE/{$OWNER_ARGUMENT}/{$REPOSITORY_NAME_ARGUMENT}"

    fun createRoute(owner: String, repositoryName: String): String =
        "$ROUTE_BASE/${Uri.encode(owner)}/${Uri.encode(repositoryName)}"
}
