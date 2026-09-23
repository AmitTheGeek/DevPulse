package com.devpulse.feature.developer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.devpulse.core.designsystem.DevPulseTheme
import com.devpulse.core.model.Developer
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
class DeveloperScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingUi() {
        setDeveloperContent(
            uiState = DeveloperUiState(
                username = "octocat",
                isRefreshing = true,
            ),
        )

        composeRule.onNodeWithText("Loading developer").assertIsDisplayed()
    }

    @Test
    fun developerContent() {
        setDeveloperContent(
            uiState = DeveloperUiState(
                username = "octocat",
                developer = sampleDeveloper(),
                repositories = listOf(sampleRepository(isSaved = true)),
            ),
        )

        composeRule.onNodeWithText("The Octocat").assertIsDisplayed()
        composeRule.onNodeWithText("@octocat").assertIsDisplayed()
        composeRule.onNodeWithText("GitHub mascot").assertIsDisplayed()
        composeRule.onNodeWithText("Followers").assertIsDisplayed()
        composeRule.onNodeWithText("Hello-World").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("80").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Kotlin").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Saved").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun emptyRepositoryState() {
        setDeveloperContent(
            uiState = DeveloperUiState(
                username = "octocat",
                developer = sampleDeveloper(),
                repositories = emptyList(),
            ),
        )

        composeRule.onNodeWithText("No repositories cached").assertIsDisplayed()
    }

    @Test
    fun initialErrorAndRetry() {
        var retried = false
        setDeveloperContent(
            uiState = DeveloperUiState(
                username = "missing",
                initialError = DeveloperUiError.NotFound,
            ),
            onRetry = { retried = true },
        )

        composeRule.onNodeWithText("Developer not found").assertIsDisplayed()
        composeRule
            .onNodeWithTag(DeveloperTestTags.RETRY_BUTTON)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(retried)
        }
    }

    @Test
    fun cachedContentRemainsVisibleWithRefreshError() {
        setDeveloperContent(
            uiState = DeveloperUiState(
                username = "octocat",
                developer = sampleDeveloper(),
                repositories = listOf(sampleRepository()),
                refreshError = DeveloperUiError.NetworkUnavailable,
            ),
        )

        composeRule.onNodeWithText("The Octocat").assertIsDisplayed()
        composeRule.onNodeWithText("Refresh failed").assertIsDisplayed()
        composeRule.onNodeWithText("Hello-World").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(DeveloperUiError.NetworkUnavailable.message).assertIsDisplayed()
    }

    @Test
    fun repositoryRowClickEmitsRepositoryIdentity() {
        var selectedRepository: Pair<String, String>? = null
        setDeveloperContent(
            uiState = DeveloperUiState(
                username = "octocat",
                developer = sampleDeveloper(),
                repositories = listOf(sampleRepository()),
            ),
            onRepositoryClick = { owner, repositoryName ->
                selectedRepository = owner to repositoryName
            },
        )

        composeRule
            .onNodeWithTag(DeveloperTestTags.repositoryRow("octocat/Hello-World"))
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(selectedRepository == "octocat" to "Hello-World")
        }
    }

    private fun setDeveloperContent(
        uiState: DeveloperUiState,
        onRefresh: () -> Unit = {},
        onRetry: () -> Unit = {},
        onBackClick: () -> Unit = {},
        onRepositoryClick: (owner: String, repositoryName: String) -> Unit = { _, _ -> },
    ) {
        composeRule.setContent {
            DevPulseTheme {
                DeveloperScreen(
                    uiState = uiState,
                    onRefresh = onRefresh,
                    onRetry = onRetry,
                    onBackClick = onBackClick,
                    onRepositoryClick = onRepositoryClick,
                )
            }
        }
    }

    private fun sampleDeveloper(): Developer =
        Developer(
            id = 42L,
            username = "octocat",
            displayName = "The Octocat",
            avatarUrl = "https://avatars.githubusercontent.com/u/42?v=4",
            profileUrl = "https://github.com/octocat",
            bio = "GitHub mascot",
            publicRepositoryCount = 8,
            followerCount = 9001,
            followingCount = 12,
        )

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
