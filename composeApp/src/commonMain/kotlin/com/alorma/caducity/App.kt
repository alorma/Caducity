package com.alorma.caducity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alorma.caducity.navigation.NavGraph
import com.alorma.caducity.navigation.Screen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        Scaffold(
            modifier = Modifier.safeDrawingPadding(),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        selected = currentDestination?.hierarchy?.any { 
                            it.hasRoute(Screen.Dashboard::class) 
                        } == true,
                        onClick = {
                            navController.navigate(Screen.Dashboard) {
                                popUpTo(Screen.Dashboard) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentDestination?.hierarchy?.any { 
                            it.hasRoute(Screen.Settings::class) 
                        } == true,
                        onClick = {
                            navController.navigate(Screen.Settings) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NavGraph(navController = navController)
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
