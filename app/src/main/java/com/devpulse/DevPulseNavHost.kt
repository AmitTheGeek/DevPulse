package com.devpulse

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devpulse.feature.developer.DeveloperDestination
import com.devpulse.feature.developer.DeveloperRoute
import com.devpulse.feature.search.SearchDestination
import com.devpulse.feature.search.SearchRoute

@Composable
fun DevPulseNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    developerContent: @Composable (username: String, onBackClick: () -> Unit) -> Unit = { username, onBackClick ->
        DeveloperRoute(
            username = username,
            onBackClick = onBackClick,
        )
    },
) {
    NavHost(
        navController = navController,
        startDestination = SearchDestination.ROUTE,
        modifier = modifier,
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
        ) { backStackEntry ->
            val username = backStackEntry.arguments
                ?.getString(DeveloperDestination.USERNAME_ARGUMENT)
                .orEmpty()

            developerContent(
                username,
                navController::navigateUp,
            )
        }
    }
}
