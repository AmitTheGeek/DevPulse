package com.devpulse

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.devpulse.core.designsystem.DevPulseTheme
import com.devpulse.feature.saved.SavedDestination
import com.devpulse.feature.search.SearchTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DevPulseNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchUsernameNavigatesToDeveloperDestination() {
        composeRule.setContent {
            DevPulseTheme {
                DevPulseNavHost(
                    developerContent = { username, _, _ ->
                        Text(text = "Developer destination: $username")
                    },
                )
            }
        }

        composeRule
            .onNodeWithTag(SearchTestTags.USERNAME_FIELD)
            .performTextInput("octocat")
        composeRule
            .onNodeWithTag(SearchTestTags.SEARCH_BUTTON)
            .performClick()

        composeRule
            .onNodeWithText("Developer destination: octocat")
            .assertIsDisplayed()
    }

    @Test
    fun developerRepositoryClickNavigatesToRepositoryDestination() {
        composeRule.setContent {
            DevPulseTheme {
                DevPulseNavHost(
                    developerContent = { _, _, onRepositoryClick ->
                        Button(
                            onClick = { onRepositoryClick("octocat", "Hello-World") },
                            modifier = Modifier.testTag("test:developer-repository"),
                        ) {
                            Text(text = "Open repository")
                        }
                    },
                    repositoryContent = { owner, repositoryName, _ ->
                        Text(text = "Repository destination: $owner/$repositoryName")
                    },
                )
            }
        }

        composeRule
            .onNodeWithTag(SearchTestTags.USERNAME_FIELD)
            .performTextInput("octocat")
        composeRule
            .onNodeWithTag(SearchTestTags.SEARCH_BUTTON)
            .performClick()
        composeRule
            .onNodeWithTag("test:developer-repository")
            .performClick()

        composeRule
            .onNodeWithText("Repository destination: octocat/Hello-World")
            .assertIsDisplayed()
    }

    @Test
    fun savedRepositoryClickNavigatesToRepositoryDestination() {
        composeRule.setContent {
            DevPulseTheme {
                DevPulseNavHost(
                    repositoryContent = { owner, repositoryName, _ ->
                        Text(text = "Repository destination: $owner/$repositoryName")
                    },
                    savedContent = { onRepositoryClick ->
                        Button(
                            onClick = { onRepositoryClick("octocat", "Hello-World") },
                            modifier = Modifier.testTag("test:saved-repository"),
                        ) {
                            Text(text = "Open saved repository")
                        }
                    },
                )
            }
        }

        composeRule
            .onNodeWithTag(DevPulseNavigationTestTags.topLevelDestination(SavedDestination.ROUTE))
            .performClick()
        composeRule
            .onNodeWithTag("test:saved-repository")
            .performClick()

        composeRule
            .onNodeWithText("Repository destination: octocat/Hello-World")
            .assertIsDisplayed()
    }

    @Test
    fun topLevelNavigationRetainsExploreStack() {
        composeRule.setContent {
            DevPulseTheme {
                DevPulseNavHost(
                    developerContent = { username, _, _ ->
                        Text(text = "Developer destination: $username")
                    },
                    savedContent = {
                        Text(text = "Saved destination")
                    },
                )
            }
        }

        composeRule
            .onNodeWithTag(SearchTestTags.USERNAME_FIELD)
            .performTextInput("octocat")
        composeRule
            .onNodeWithTag(SearchTestTags.SEARCH_BUTTON)
            .performClick()
        composeRule
            .onNodeWithText("Developer destination: octocat")
            .assertIsDisplayed()

        composeRule
            .onNodeWithTag(DevPulseNavigationTestTags.topLevelDestination(SavedDestination.ROUTE))
            .performClick()
        composeRule
            .onNodeWithText("Saved destination")
            .assertIsDisplayed()

        composeRule
            .onNodeWithTag(DevPulseNavigationTestTags.topLevelDestination(ExploreDestination.ROUTE))
            .performClick()
        composeRule
            .onNodeWithText("Developer destination: octocat")
            .assertIsDisplayed()
    }
}
