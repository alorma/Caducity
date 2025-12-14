package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alorma.caducity.ui.screen.productdetail.ProductDetailScreen

/**
 * Adaptive screen that manages the list-detail layout pattern for the Dashboard.
 * 
 * Uses Material 3's ListDetailPaneScaffold to provide adaptive behavior:
 * - On wide screens (>= 600dp): Shows list and detail panes side-by-side
 * - On narrow screens: Shows one pane at a time with proper back navigation
 * - Handles animations and transitions automatically
 * 
 * The scaffold automatically determines the layout based on screen size using
 * Material Design's adaptive layout guidelines.
 */
@Composable
fun DashboardAdaptiveScreen() {
  val navigator = rememberListDetailPaneScaffoldNavigator<String>()
  val adaptiveInfo = currentWindowAdaptiveInfo()

  ListDetailPaneScaffold(
    directive = navigator.scaffoldDirective,
    value = navigator.scaffoldValue,
    listPane = {
      AnimatedPane {
        DashboardScreen(
          onNavigateToProductDetail = { productId ->
            // Navigate to detail pane within the scaffold
            // On wide screens, this will show the detail pane alongside the list
            // On narrow screens, this will show the detail pane and hide the list
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, productId)
          }
        )
      }
    },
    detailPane = {
      AnimatedPane {
        // Get the selected product ID from the navigator's current destination
        val selectedProductId = navigator.currentDestination?.content
        selectedProductId?.let { productId ->
          ProductDetailScreen(
            productId = productId,
            onBack = {
              // Navigate back to list pane
              navigator.navigateBack()
            }
          )
        } ?: Box(modifier = Modifier.fillMaxSize()) {
          // Empty detail pane placeholder when nothing is selected
          // On wide screens, this shows an empty state
          // On narrow screens, this pane is hidden
        }
      }
    }
  )
}
