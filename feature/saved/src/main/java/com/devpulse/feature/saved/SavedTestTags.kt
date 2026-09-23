package com.devpulse.feature.saved

object SavedTestTags {
    fun repositoryRow(fullName: String): String = "saved:repository:$fullName"
    fun unsaveButton(repositoryId: Long): String = "saved:unsave:$repositoryId"
}
