package com.devpulse.feature.saved

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
class SavedScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun contentList() {
        setSavedContent(
            uiState = SavedUiState(
                repositories = listOf(sampleRepository()),
            ),
        )

        composeRule.onNodeWithText("Saved").assertIsDisplayed()
        composeRule.onNodeWithText("Hello-World").assertIsDisplayed()
        composeRule.onNodeWithText("octocat/Hello-World").assertIsDisplayed()
        composeRule.onNodeWithText("80").assertIsDisplayed()
        composeRule.onNodeWithText("Kotlin").assertIsDisplayed()
    }

    @Test
    fun emptyState() {
        setSavedContent(uiState = SavedUiState())

        composeRule.onNodeWithText("No saved repositories").assertIsDisplayed()
    }

    @Test
    fun unsaveInteraction() {
        var unsavedId: Long? = null
        setSavedContent(
            uiState = SavedUiState(
                repositories = listOf(sampleRepository()),
            ),
            onUnsave = { repositoryId -> unsavedId = repositoryId },
        )

        composeRule
            .onNodeWithTag(SavedTestTags.unsaveButton(1296269L))
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(unsavedId == 1296269L)
        }
    }

    @Test
    fun repositoryClickEmitsIdentity() {
        var selectedRepository: Pair<String, String>? = null
        setSavedContent(
            uiState = SavedUiState(
                repositories = listOf(sampleRepository()),
            ),
            onRepositoryClick = { owner, repositoryName ->
                selectedRepository = owner to repositoryName
            },
        )

        composeRule
            .onNodeWithTag(SavedTestTags.repositoryRow("octocat/Hello-World"))
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(selectedRepository == "octocat" to "Hello-World")
        }
    }

    private fun setSavedContent(
        uiState: SavedUiState,
        onRepositoryClick: (owner: String, repositoryName: String) -> Unit = { _, _ -> },
        onUnsave: (repositoryId: Long) -> Unit = {},
    ) {
        composeRule.setContent {
            DevPulseTheme {
                SavedScreen(
                    uiState = uiState,
                    onRepositoryClick = onRepositoryClick,
                    onUnsave = onUnsave,
                )
            }
        }
    }

    private fun sampleRepository(): Repository =
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
        )
}
