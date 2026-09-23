package com.devpulse

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devpulse.feature.developer.DeveloperDestination
import com.devpulse.feature.developer.DeveloperRoute
import com.devpulse.feature.repository.RepositoryDestination
import com.devpulse.feature.repository.RepositoryRoute
import com.devpulse.feature.saved.SavedDestination
import com.devpulse.feature.saved.SavedRoute
import com.devpulse.feature.search.SearchDestination
import com.devpulse.feature.search.SearchRoute

private typealias DeveloperContent = @Composable (
    username: String,
    onBackClick: () -> Unit,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
) -> Unit

private typealias RepositoryContent = @Composable (
    owner: String,
    repositoryName: String,
    onBackClick: () -> Unit,
) -> Unit

private typealias SavedContent = @Composable (
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
) -> Unit

private class DevPulseNavigationContent(
    val developer: DeveloperContent,
    val repository: RepositoryContent,
    val saved: SavedContent,
)

@Composable
fun DevPulseNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    developerContent: DeveloperContent = { _, onBackClick, onRepositoryClick ->
        DeveloperRoute(
            onBackClick = onBackClick,
            onRepositoryClick = onRepositoryClick,
        )
    },
    repositoryContent: RepositoryContent = { _, _, onBackClick ->
        RepositoryRoute(
            onBackClick = onBackClick,
        )
    },
    savedContent: SavedContent = { onRepositoryClick ->
        SavedRoute(
            onRepositoryClick = onRepositoryClick,
        )
    },
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedTopLevelRoute = rememberSelectedTopLevelRoute(currentDestination)
    val navigationContent = DevPulseNavigationContent(
        developer = developerContent,
        repository = repositoryContent,
        saved = savedContent,
    )
    val navigateToRepository = { owner: String, repositoryName: String ->
        navController.navigate(RepositoryDestination.createRoute(owner, repositoryName))
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            DevPulseNavigationBar(
                selectedTopLevelRoute = selectedTopLevelRoute,
                onDestinationClick = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        DevPulseNavigationGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            content = navigationContent,
            onRepositoryClick = navigateToRepository,
        )
    }
}

@Composable
private fun rememberSelectedTopLevelRoute(
    currentDestination: NavDestination?,
): String {
    var selectedTopLevelRoute by rememberSaveable {
        mutableStateOf(ExploreDestination.ROUTE)
    }

    LaunchedEffect(currentDestination?.route) {
        when {
            currentDestination.isInHierarchy(ExploreDestination.ROUTE) -> {
                selectedTopLevelRoute = ExploreDestination.ROUTE
            }
            currentDestination?.route == SavedDestination.ROUTE -> {
                selectedTopLevelRoute = SavedDestination.ROUTE
            }
        }
    }

    return selectedTopLevelRoute
}

@Composable
private fun DevPulseNavigationGraph(
    navController: NavHostController,
    modifier: Modifier,
    content: DevPulseNavigationContent,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = ExploreDestination.ROUTE,
        modifier = modifier,
    ) {
        exploreGraph(navController, content.developer, onRepositoryClick)
        repositoryDestination(navController, content.repository)
        savedDestination(content.saved, onRepositoryClick)
    }
}

private fun NavGraphBuilder.exploreGraph(
    navController: NavHostController,
    developerContent: DeveloperContent,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
) {
    navigation(
        startDestination = SearchDestination.ROUTE,
        route = ExploreDestination.ROUTE,
    ) {
        composable(SearchDestination.ROUTE) {
            SearchRoute(
                onDeveloperSearch = { username ->
                    navController.navigate(DeveloperDestination.createRoute(username))
                },
            )
        }

        composable(
            route = DeveloperDestination.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(DeveloperDestination.USERNAME_ARGUMENT) {
                    type = NavType.StringType
                },
            ),
        ) { developerBackStackEntry ->
            val username = developerBackStackEntry.arguments
                ?.getString(DeveloperDestination.USERNAME_ARGUMENT)
                .orEmpty()

            developerContent(
                username,
                navController::navigateUp,
                onRepositoryClick,
            )
        }
    }
}

private fun NavGraphBuilder.repositoryDestination(
    navController: NavHostController,
    repositoryContent: RepositoryContent,
) {
    composable(
        route = RepositoryDestination.ROUTE_PATTERN,
        arguments = listOf(
            navArgument(RepositoryDestination.OWNER_ARGUMENT) {
                type = NavType.StringType
            },
            navArgument(RepositoryDestination.REPOSITORY_NAME_ARGUMENT) {
                type = NavType.StringType
            },
        ),
    ) { repositoryBackStackEntry ->
        val owner = repositoryBackStackEntry.arguments
            ?.getString(RepositoryDestination.OWNER_ARGUMENT)
            .orEmpty()
        val repositoryName = repositoryBackStackEntry.arguments
            ?.getString(RepositoryDestination.REPOSITORY_NAME_ARGUMENT)
            .orEmpty()

        repositoryContent(
            owner,
            repositoryName,
            navController::navigateUp,
        )
    }
}

private fun NavGraphBuilder.savedDestination(
    savedContent: SavedContent,
    onRepositoryClick: (owner: String, repositoryName: String) -> Unit,
) {
    composable(SavedDestination.ROUTE) {
        savedContent(onRepositoryClick)
    }
}

@Composable
private fun DevPulseNavigationBar(
    selectedTopLevelRoute: String,
    onDestinationClick: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedTopLevelRoute == destination.route,
                onClick = { onDestinationClick(destination) },
                modifier = Modifier.testTag(
                    DevPulseNavigationTestTags.topLevelDestination(destination.route),
                ),
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = {
                    Text(text = destination.label)
                },
            )
        }
    }
}

private fun NavDestination?.isInHierarchy(route: String): Boolean =
    this?.hierarchy?.any { destination -> destination.route == route } == true

private enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Explore(
        route = ExploreDestination.ROUTE,
        label = "Explore",
        icon = Icons.Outlined.Explore,
    ),
    Saved(
        route = SavedDestination.ROUTE,
        label = "Saved",
        icon = Icons.Outlined.Bookmark,
    ),
}
