package com.alorma.caducity.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.NavHostController
import androidx.navigation3.ui.compose.NavHost
import androidx.navigation3.ui.compose.composable
import com.alorma.caducity.ui.screens.create.CreateProductScreen
import com.alorma.caducity.ui.screens.dashboard.DashboardScreen
import com.alorma.caducity.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard
    ) {
        composable<Screen.Dashboard> {
            DashboardScreen(
                onNavigateToCreateProduct = {
                    navController.navigate(Screen.CreateProduct)
                }
            )
        }
        
        composable<Screen.CreateProduct> {
            CreateProductScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable<Screen.Settings> {
            SettingsScreen()
        }
    }
}
