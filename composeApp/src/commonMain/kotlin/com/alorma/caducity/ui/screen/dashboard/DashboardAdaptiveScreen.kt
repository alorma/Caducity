package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.alorma.caducity.ui.screen.productdetail.ProductDetailScreen
import com.alorma.caducity.ui.theme.CaducityTheme

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
        } ?: EmptyDetailPane()
      }
    }
  )
}

/**
 * Empty state shown in the detail pane when no product is selected.
 * Only visible on wide screens where both panes are shown simultaneously.
 */
@Composable
private fun EmptyDetailPane() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = "Select a product to view details",
      style = MaterialTheme.typography.bodyLarge,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
  }
}
