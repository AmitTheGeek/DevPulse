package com.devpulse.feature.search

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.devpulse.core.designsystem.DevPulseTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchUsernameSubmission() {
        var submittedUsername: String? = null

        composeRule.setContent {
            DevPulseTheme {
                SearchRoute(
                    onDeveloperSearch = { username ->
                        submittedUsername = username
                    },
                )
            }
        }

        composeRule
            .onNodeWithTag(SearchTestTags.USERNAME_FIELD)
            .performTextInput(" octocat ")
        composeRule
            .onNodeWithTag(SearchTestTags.SEARCH_BUTTON)
            .performClick()

        composeRule.runOnIdle {
            assertEquals("octocat", submittedUsername)
        }
    }
}
