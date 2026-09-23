package com.devpulse.feature.repository

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class RepositoryUpdatedAtFormatter @Inject constructor() {
    private val formatter = DateTimeFormatter
        .ofPattern("MMM d, yyyy", Locale.US)
        .withZone(ZoneOffset.UTC)

    fun format(updatedAt: Instant?): String =
        updatedAt?.let { "Updated ${formatter.format(it)}" } ?: "Updated time unavailable"
}
