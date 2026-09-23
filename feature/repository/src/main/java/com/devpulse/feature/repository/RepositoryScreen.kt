package com.devpulse.feature.repository

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devpulse.core.designsystem.DevPulseTheme
import com.devpulse.core.model.Repository
import java.time.Instant

@Composable
fun RepositoryRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RepositoryViewModel = hiltViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RepositoryScreen(
        uiState = uiState,
        actions = RepositoryScreenActions(
            onRefresh = viewModel::onRefresh,
            onRetry = viewModel::onRetry,
            onToggleSaved = viewModel::onToggleSaved,
            onOpenGitHub = { url -> openGitHubUrl(url, uriHandler) },
            onBackClick = onBackClick,
        ),
        modifier = modifier,
    )
}

@Composable
fun RepositoryScreen(
    uiState: RepositoryUiState,
    actions: RepositoryScreenActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        RepositoryTopBar(
            title = "${uiState.owner}/${uiState.repositoryName}",
            isRefreshing = uiState.isRefreshing,
            onBackClick = actions.onBackClick,
            onRefresh = actions.onRefresh,
        )

        if (uiState.isRefreshing && uiState.hasContent) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        when {
            uiState.isInitialLoading -> LoadingContent()
            uiState.isInitialError -> InitialErrorContent(
                error = uiState.initialError,
                onRetry = actions.onRetry,
            )
            uiState.hasContent -> RepositoryContent(
                uiState = uiState,
                onToggleSaved = actions.onToggleSaved,
                onOpenGitHub = actions.onOpenGitHub,
            )
            else -> InitialErrorContent(
                error = RepositoryUiError.NotFound,
                onRetry = actions.onRetry,
            )
        }
    }
}

@Composable
private fun RepositoryTopBar(
    title: String,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
            modifier = Modifier.testTag(RepositoryTestTags.REFRESH_BUTTON),
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Refresh",
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading repository",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun InitialErrorContent(
    error: RepositoryUiError?,
    onRetry: () -> Unit,
) {
    ErrorPanel(
        title = error?.title ?: RepositoryUiError.Unknown.title,
        message = error?.message ?: RepositoryUiError.Unknown.message,
        action = {
            Button(
                onClick = onRetry,
                modifier = Modifier.testTag(RepositoryTestTags.RETRY_BUTTON),
            ) {
                Text(text = "Retry")
            }
        },
    )
}

@Composable
private fun RepositoryContent(
    uiState: RepositoryUiState,
    onToggleSaved: () -> Unit,
    onOpenGitHub: (String) -> Unit,
) {
    val repository = uiState.repository ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RepositoryHeader(repository = repository)
        }

        uiState.refreshError?.let { error ->
            item {
                ErrorBanner(
                    title = "Refresh failed",
                    message = error.message,
                )
            }
        }

        uiState.saveError?.let { error ->
            item {
                ErrorBanner(
                    title = "Save failed",
                    message = error.message,
                )
            }
        }

        item {
            RepositoryStats(repository = repository)
        }

        item {
            RepositoryMetadata(
                repository = repository,
                updatedAtLabel = uiState.updatedAtLabel,
            )
        }

        item {
            RepositoryActions(
                repository = repository,
                isSaving = uiState.isSaving,
                onToggleSaved = onToggleSaved,
                onOpenGitHub = onOpenGitHub,
            )
        }
    }
}

@Composable
private fun RepositoryHeader(repository: Repository) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = repository.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = repository.fullName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        repository.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        RepositoryBadges(repository = repository)
    }
}

@Composable
private fun RepositoryBadges(repository: Repository) {
    val badges = buildList {
        if (repository.isPrivate) add("Private")
        if (repository.isFork) add("Fork")
        if (repository.isArchived) add("Archived")
    }
    if (badges.isEmpty()) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        badges.forEach { badge ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun RepositoryStats(repository: Repository) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatBlock(
            label = "Stars",
            value = repository.starCount.toString(),
            modifier = Modifier.weight(1f),
        )
        StatBlock(
            label = "Forks",
            value = repository.forkCount.toString(),
            modifier = Modifier.weight(1f),
        )
        StatBlock(
            label = "Issues",
            value = repository.openIssueCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RepositoryMetadata(
    repository: Repository,
    updatedAtLabel: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MetadataRow(
            label = "Language",
            value = repository.primaryLanguage ?: "Not specified",
        )
        MetadataRow(
            label = "Updated",
            value = updatedAtLabel.removePrefix("Updated "),
        )
    }
}

@Composable
private fun RepositoryActions(
    repository: Repository,
    isSaving: Boolean,
    onToggleSaved: () -> Unit,
    onOpenGitHub: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onToggleSaved,
            enabled = !isSaving,
            modifier = Modifier.testTag(RepositoryTestTags.SAVE_BUTTON),
        ) {
            Icon(
                imageVector = if (repository.isSaved) {
                    Icons.Outlined.Bookmark
                } else {
                    Icons.Outlined.BookmarkBorder
                },
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (repository.isSaved) "Unsave" else "Save")
        }
        OutlinedButton(
            onClick = { onOpenGitHub(repository.url) },
            modifier = Modifier.testTag(RepositoryTestTags.OPEN_GITHUB_BUTTON),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "GitHub")
        }
    }
}

@Composable
private fun StatBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ErrorBanner(
    title: String,
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ErrorPanel(
    title: String,
    message: String,
    action: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        action()
    }
}

private fun openGitHubUrl(url: String, uriHandler: UriHandler) {
    if (!url.startsWith("https://github.com/") && !url.startsWith("http://github.com/")) return
    runCatching { uriHandler.openUri(url) }
}

@Preview
@Composable
private fun RepositoryScreenPreview() {
    DevPulseTheme {
        RepositoryScreen(
            uiState = RepositoryUiState(
                owner = "octocat",
                repositoryName = "Hello-World",
                repository = Repository(
                    id = 1296269L,
                    ownerUsername = "octocat",
                    name = "Hello-World",
                    fullName = "octocat/Hello-World",
                    description = "My first repository on GitHub.",
                    url = "https://github.com/octocat/Hello-World",
                    primaryLanguage = "Kotlin",
                    starCount = 80,
                    forkCount = 9,
                    openIssueCount = 3,
                    isFork = false,
                    isArchived = false,
                    isPrivate = false,
                    isSaved = true,
                    updatedAt = Instant.parse("2026-09-08T09:30:00Z"),
                ),
                updatedAtLabel = "Updated Sep 8, 2026",
            ),
            actions = RepositoryScreenActions(
                onRefresh = {},
                onRetry = {},
                onToggleSaved = {},
                onOpenGitHub = {},
                onBackClick = {},
            ),
        )
    }
}
