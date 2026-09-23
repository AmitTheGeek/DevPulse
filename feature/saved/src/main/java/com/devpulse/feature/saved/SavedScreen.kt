package com.devpulse.feature.saved

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

object SavedTestTags {
    fun repositoryRow(fullName: String): String = "saved:repository:$fullName"
    fun unsaveButton(repositoryId: Long): String = "saved:unsave:$repositoryId"
}

@Composable
fun SavedRoute(
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SavedScreen(
        uiState = uiState,
        onRepositoryClick = onRepositoryClick,
        onUnsave = viewModel::onUnsave,
        modifier = modifier,
    )
}

@Composable
fun SavedScreen(
    uiState: SavedUiState,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
    onUnsave: (repositoryId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Text(
            text = "Saved",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
        )

        when {
            uiState.isLoading -> LoadingContent()
            uiState.isEmpty -> EmptySavedContent()
            else -> SavedContent(
                uiState = uiState,
                onRepositoryClick = onRepositoryClick,
                onUnsave = onUnsave,
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading saved repositories",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun EmptySavedContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No saved repositories",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Repositories you save will appear here for quick access.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SavedContent(
    uiState: SavedUiState,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
    onUnsave: (repositoryId: Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        uiState.actionError?.let { error ->
            item {
                ErrorBanner(message = error.message)
            }
        }

        items(
            items = uiState.repositories,
            key = { repository -> repository.id },
        ) { repository ->
            SavedRepositoryRow(
                repository = repository,
                isUpdating = uiState.updatingRepositoryId == repository.id,
                onRepositoryClick = onRepositoryClick,
                onUnsave = onUnsave,
            )
        }
    }
}

@Composable
private fun SavedRepositoryRow(
    repository: Repository,
    isUpdating: Boolean,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
    onUnsave: (repositoryId: Long) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRepositoryClick(repository.ownerUsername, repository.name) }
            .testTag(SavedTestTags.repositoryRow(repository.fullName)),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = repository.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = repository.fullName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = repository.starCount.toString(),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    repository.primaryLanguage?.let { language ->
                        Text(
                            text = language,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            IconButton(
                onClick = { onUnsave(repository.id) },
                enabled = !isUpdating,
                modifier = Modifier.testTag(SavedTestTags.unsaveButton(repository.id)),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bookmark,
                    contentDescription = "Unsave",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
private fun SavedScreenPreview() {
    DevPulseTheme {
        SavedScreen(
            uiState = SavedUiState(
                repositories = listOf(
                    Repository(
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
                ),
            ),
            onRepositoryClick = { _, _ -> },
            onUnsave = {},
        )
    }
}
