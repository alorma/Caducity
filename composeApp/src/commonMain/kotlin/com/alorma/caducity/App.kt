package com.alorma.caducity

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.NavDestination.Companion.hasRoute
import androidx.navigation3.NavDestination.Companion.hierarchy
import androidx.navigation3.ui.compose.currentBackStackEntryAsState
import androidx.navigation3.ui.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import com.alorma.caducity.navigation.NavGraph
import com.alorma.caducity.navigation.Screen
import com.alorma.caducity.ui.icons.AppIcons
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        // Get adaptive info to determine the appropriate navigation type
        val adaptiveInfo = currentWindowAdaptiveInfo()
        val customNavSuiteType = when (adaptiveInfo.windowSizeClass.windowWidthSizeClass) {
            WindowWidthSizeClass.COMPACT -> NavigationSuiteType.NavigationBar
            WindowWidthSizeClass.MEDIUM -> NavigationSuiteType.NavigationRail
            WindowWidthSizeClass.EXPANDED -> NavigationSuiteType.NavigationDrawer
            else -> NavigationSuiteType.NavigationBar
        }

        NavigationSuiteScaffold(
            modifier = Modifier.safeDrawingPadding(),
            layoutType = customNavSuiteType,
            navigationSuiteItems = {
                item(
                    icon = { Icon(AppIcons.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(Screen.Dashboard::class)
                    } == true,
                    onClick = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                item(
                    icon = { Icon(AppIcons.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(Screen.Settings::class)
                    } == true,
                    onClick = {
                        navController.navigate(Screen.Settings) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) {
            NavGraph(navController = navController)
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
