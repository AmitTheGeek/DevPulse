package com.devpulse.core.data.cache

internal object SyncKeys {
    fun developer(username: String): String = "developer:${username.cacheKeyPart()}"

    fun repositories(username: String): String = "repositories:${username.cacheKeyPart()}"

    fun repository(owner: String, repositoryName: String): String =
        "repository:${owner.cacheKeyPart()}/${repositoryName.cacheKeyPart()}"

    private fun String.cacheKeyPart(): String = trim().lowercase()
}

