package com.devpulse.feature.developer

object DeveloperTestTags {
    const val REFRESH_BUTTON = "developer:refresh"
    const val RETRY_BUTTON = "developer:retry"

    fun repositoryRow(fullName: String): String = "developer:repository:$fullName"
}
