package com.devpulse.feature.repository

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.devpulse.core.designsystem.DevPulseTheme
import com.devpulse.core.model.Repository
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepositoryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun content() {
        setRepositoryContent(
            uiState = RepositoryUiState(
                owner = "octocat",
                repositoryName = "Hello-World",
                repository = sampleRepository(isSaved = true),
                updatedAtLabel = "Updated Sep 8, 2026",
            ),
        )

        composeRule.onNodeWithText("Hello-World").assertIsDisplayed()
        composeRule.onNodeWithText("My first repository on GitHub.").assertIsDisplayed()
        composeRule.onNodeWithText("Stars").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Forks").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Issues").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Kotlin").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Unsave").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun loading() {
        setRepositoryContent(
            uiState = RepositoryUiState(
                owner = "octocat",
                repositoryName = "Hello-World",
                isRefreshing = true,
            ),
        )

        composeRule.onNodeWithText("Loading repository").assertIsDisplayed()
    }

    @Test
    fun errorAndRetry() {
        var retried = false
        setRepositoryContent(
            uiState = RepositoryUiState(
                owner = "octocat",
                repositoryName = "missing",
                initialError = RepositoryUiError.NotFound,
            ),
            onRetry = { retried = true },
        )

        composeRule.onNodeWithText("Repository not found").assertIsDisplayed()
        composeRule.onNodeWithTag(RepositoryTestTags.RETRY_BUTTON).performClick()

        composeRule.runOnIdle {
            assertTrue(retried)
        }
    }

    @Test
    fun cachedContentWithRefreshFailure() {
        setRepositoryContent(
            uiState = RepositoryUiState(
                owner = "octocat",
                repositoryName = "Hello-World",
                repository = sampleRepository(),
                updatedAtLabel = "Updated Sep 8, 2026",
                refreshError = RepositoryUiError.NetworkUnavailable,
            ),
        )

        composeRule.onNodeWithText("Hello-World").assertIsDisplayed()
        composeRule.onNodeWithText("Refresh failed").assertIsDisplayed()
        composeRule.onNodeWithText(RepositoryUiError.NetworkUnavailable.message).assertIsDisplayed()
    }

    @Test
    fun saveInteraction() {
        var toggled = false
        setRepositoryContent(
            uiState = RepositoryUiState(
                owner = "octocat",
                repositoryName = "Hello-World",
                repository = sampleRepository(isSaved = false),
            ),
            onToggleSaved = { toggled = true },
        )

        composeRule
            .onNodeWithTag(RepositoryTestTags.SAVE_BUTTON)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(toggled)
        }
    }

    @Test
    fun unsaveInteraction() {
        var toggled = false
        setRepositoryContent(
            uiState = RepositoryUiState(
                owner = "octocat",
                repositoryName = "Hello-World",
                repository = sampleRepository(isSaved = true),
            ),
            onToggleSaved = { toggled = true },
        )

        composeRule
            .onNodeWithTag(RepositoryTestTags.SAVE_BUTTON)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(toggled)
        }
    }

    private fun setRepositoryContent(
        uiState: RepositoryUiState,
        onRefresh: () -> Unit = {},
        onRetry: () -> Unit = {},
        onToggleSaved: () -> Unit = {},
        onOpenGitHub: (String) -> Unit = {},
        onBackClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            DevPulseTheme {
                RepositoryScreen(
                    uiState = uiState,
                    actions = RepositoryScreenActions(
                        onRefresh = onRefresh,
                        onRetry = onRetry,
                        onToggleSaved = onToggleSaved,
                        onOpenGitHub = onOpenGitHub,
                        onBackClick = onBackClick,
                    ),
                )
            }
        }
    }

    private fun sampleRepository(
        isSaved: Boolean = false,
    ): Repository =
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
            isSaved = isSaved,
            updatedAt = Instant.parse("2026-09-08T09:30:00Z"),
        )
}
