package com.devpulse.feature.repository

class RepositoryScreenActions(
    val onRefresh: () -> Unit,
    val onRetry: () -> Unit,
    val onToggleSaved: () -> Unit,
    val onOpenGitHub: (String) -> Unit,
    val onBackClick: () -> Unit,
)
