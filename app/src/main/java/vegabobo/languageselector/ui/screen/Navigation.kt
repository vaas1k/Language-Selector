package vegabobo.languageselector.ui.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import vegabobo.languageselector.ui.screen.Destinations.ABOUT
import vegabobo.languageselector.ui.screen.Destinations.APP_INFO
import vegabobo.languageselector.ui.screen.Destinations.HOME
import vegabobo.languageselector.ui.screen.Destinations.LIBRARIES
import vegabobo.languageselector.ui.screen.about.AboutScreen
import vegabobo.languageselector.ui.screen.about.LibrariesScreen
import vegabobo.languageselector.ui.screen.appinfo.AppInfoScreen
import vegabobo.languageselector.ui.screen.main.MainScreen
import vegabobo.languageselector.ui.theme.Ui

object Destinations {
    const val HOME = "home"
    const val APP_INFO = "app_info"
    const val ABOUT = "about"
    const val LIBRARIES = "libraries"
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HOME,
        enterTransition = { Ui.enter },
        exitTransition = { Ui.exit },
        popEnterTransition = { Ui.enter },
        popExitTransition = { Ui.exit }
    ) {
        composable(
            route = HOME
        ) {
            MainScreen(
                navigateToAppScreen = { navController.navigate("$APP_INFO/$it") },
                navigateToAbout = { navController.navigate(ABOUT)}
            )
        }

        composable(
            route = "$APP_INFO/{app_id}",
            arguments = listOf(navArgument("app_id") { type = NavType.StringType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("app_id") ?: return@composable
            AppInfoScreen(appId = appId, navigateBack = { navController.navigateUp() })
        }

        composable(route = ABOUT) {
            AboutScreen(
                navigateBack = { navController.navigateUp() },
                navigateToLibraries = { navController.navigate(LIBRARIES) }
            )
        }

        composable(route = LIBRARIES) {
            LibrariesScreen(navigateBack = { navController.navigateUp() })
        }
    }
}