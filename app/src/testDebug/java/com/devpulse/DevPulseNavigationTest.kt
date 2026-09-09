package com.devpulse

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.devpulse.core.designsystem.DevPulseTheme
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
                    developerContent = { username, _ ->
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
}
