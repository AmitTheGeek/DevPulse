package com.devpulse.feature.developer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.devpulse.core.designsystem.DevPulseTheme
import com.devpulse.core.model.Developer
import com.devpulse.core.model.Repository
import java.time.Instant

@Composable
fun DeveloperRoute(
    onBackClick: () -> Unit,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeveloperViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DeveloperScreen(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onBackClick = onBackClick,
        onRepositoryClick = onRepositoryClick,
        modifier = modifier,
    )
}

@Composable
fun DeveloperScreen(
    uiState: DeveloperUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        DeveloperTopBar(
            username = uiState.username,
            isRefreshing = uiState.isRefreshing,
            onBackClick = onBackClick,
            onRefresh = onRefresh,
        )

        if (uiState.isRefreshing && uiState.hasContent) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        when {
            uiState.isInitialLoading -> LoadingContent()
            uiState.isInitialError -> InitialErrorContent(
                error = uiState.initialError,
                onRetry = onRetry,
            )
            uiState.hasContent -> DeveloperContent(
                uiState = uiState,
                onRefresh = onRefresh,
                onRepositoryClick = onRepositoryClick,
            )
        }
    }
}

@Composable
private fun DeveloperTopBar(
    username: String,
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
            text = username,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
            modifier = Modifier.testTag(DeveloperTestTags.REFRESH_BUTTON),
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading developer",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun InitialErrorContent(
    error: DeveloperUiError?,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        ErrorPanel(
            title = error?.title ?: DeveloperUiError.Unknown.title,
            message = error?.message ?: DeveloperUiError.Unknown.message,
            action = {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.testTag(DeveloperTestTags.RETRY_BUTTON),
                ) {
                    Text(text = "Retry")
                }
            },
        )
    }
}

@Composable
private fun DeveloperContent(
    uiState: DeveloperUiState,
    onRefresh: () -> Unit,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
) {
    val developer = uiState.developer ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DeveloperHeader(developer = developer)
        }

        uiState.refreshError?.let { error ->
            item {
                RefreshErrorBanner(error = error)
            }
        }

        if (uiState.repositories.isEmpty()) {
            item {
                EmptyRepositories(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                )
            }
        } else {
            items(
                items = uiState.repositories,
                key = { repository -> repository.id },
            ) { repository ->
                RepositoryListItem(
                    repository = repository,
                    onRepositoryClick = onRepositoryClick,
                )
            }
        }
    }
}

@Composable
private fun DeveloperHeader(
    developer: Developer,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = developer.avatarUrl,
                contentDescription = "${developer.username} avatar",
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = developer.displayName ?: developer.username,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "@${developer.username}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        developer.bio?.takeIf { it.isNotBlank() }?.let { bio ->
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatBlock(
                label = "Followers",
                value = developer.followerCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatBlock(
                label = "Following",
                value = developer.followingCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatBlock(
                label = "Repos",
                value = developer.publicRepositoryCount.toString(),
                modifier = Modifier.weight(1f),
            )
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
private fun RefreshErrorBanner(
    error: DeveloperUiError,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Refresh failed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun EmptyRepositories(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    ErrorPanel(
        title = "No repositories cached",
        message = "This developer has no repository data available locally yet.",
        action = {
            Button(
                onClick = onRefresh,
                enabled = !isRefreshing,
            ) {
                Text(text = "Refresh")
            }
        },
    )
}

@Composable
private fun RepositoryListItem(
    repository: Repository,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRepositoryClick(repository.ownerUsername, repository.name) }
            .testTag(DeveloperTestTags.repositoryRow(repository.fullName)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
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
                }
                Spacer(modifier = Modifier.width(12.dp))
                SavedIndicator(isSaved = repository.isSaved)
            }

            repository.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
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
    }
}

@Composable
private fun SavedIndicator(
    isSaved: Boolean,
) {
    val color = if (isSaved) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSaved) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Bookmark,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isSaved) "Saved" else "Not saved",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
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
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        action()
    }
}

@Preview
@Composable
private fun DeveloperScreenPreview() {
    DevPulseTheme {
        DeveloperScreen(
            uiState = DeveloperUiState(
                username = "octocat",
                developer = Developer(
                    id = 1,
                    username = "octocat",
                    displayName = "The Octocat",
                    avatarUrl = "https://avatars.githubusercontent.com/u/583231?v=4",
                    profileUrl = "https://github.com/octocat",
                    bio = "GitHub mascot",
                    publicRepositoryCount = 8,
                    followerCount = 9001,
                    followingCount = 12,
                ),
                repositories = listOf(
                    Repository(
                        id = 1,
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
            onRefresh = {},
            onRetry = {},
            onBackClick = {},
            onRepositoryClick = { _, _ -> },
        )
    }
}
