package com.devpulse.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpulse.core.designsystem.DevPulseTheme

object SearchDestination {
    const val ROUTE = "search"
}

object SearchTestTags {
    const val USERNAME_FIELD = "search:username"
    const val SEARCH_BUTTON = "search:submit"
}

@Composable
fun SearchRoute(
    onDeveloperSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    fun submit() {
        val normalizedUsername = username.trim()
        val error = validateGitHubUsername(normalizedUsername)
        validationError = error

        if (error == null) {
            onDeveloperSearch(normalizedUsername)
        }
    }

    SearchScreen(
        username = username,
        onUsernameChange = {
            username = it
            validationError = null
        },
        validationError = validationError,
        onSearch = ::submit,
        modifier = modifier,
    )
}

@Composable
fun SearchScreen(
    username: String,
    onUsernameChange: (String) -> Unit,
    validationError: String?,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "DevPulse",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Find a GitHub developer",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag(SearchTestTags.USERNAME_FIELD),
                label = { Text(text = "GitHub username") },
                singleLine = true,
                isError = validationError != null,
                supportingText = {
                    validationError?.let { Text(text = it) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch() },
                ),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onSearch,
                modifier = Modifier
                    .height(56.dp)
                    .testTag(SearchTestTags.SEARCH_BUTTON),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Search")
            }
        }
    }
}

private val GitHubUsernameRegex =
    Regex("^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$")

private fun validateGitHubUsername(username: String): String? =
    when {
        username.isBlank() -> "Enter a username"
        !GitHubUsernameRegex.matches(username) -> "Use letters, numbers, or single hyphens"
        else -> null
    }

@Preview
@Composable
private fun SearchScreenPreview() {
    DevPulseTheme {
        SearchScreen(
            username = "octocat",
            onUsernameChange = {},
            validationError = null,
            onSearch = {},
        )
    }
}
