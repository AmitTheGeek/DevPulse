package com.devpulse.feature.developer

import android.net.Uri

object DeveloperDestination {
    const val ROUTE_BASE = "developer"
    const val USERNAME_ARGUMENT = "username"
    const val ROUTE_PATTERN = "$ROUTE_BASE/{$USERNAME_ARGUMENT}"

    fun createRoute(username: String): String =
        "$ROUTE_BASE/${Uri.encode(username)}"
}
