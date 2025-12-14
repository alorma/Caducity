package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import com.alorma.caducity.ui.screen.productdetail.ProductDetailScreen

/**
 * Adaptive screen that manages the list-detail layout pattern for the Dashboard.
 * On wide screens (>= 600dp), shows the dashboard list and product detail side-by-side.
 * On narrow screens, requires navigation stack to show detail (handled by App.kt navigation).
 */
@Composable
fun DashboardAdaptiveScreen(
  onNavigateToProductDetail: (String) -> Unit,
) {
  val adaptiveInfo = currentWindowAdaptiveInfo()
  val windowSizeClass = adaptiveInfo.windowSizeClass
  
  // Check if we should show list-detail side-by-side (medium or larger screens)
  val showListDetail = windowSizeClass.isWidthAtLeastBreakpoint(
    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
  )
  
  var selectedProductId by rememberSaveable { mutableStateOf<String?>(null) }

  if (showListDetail) {
    // Two-pane layout for medium/large screens
    Row(modifier = Modifier.fillMaxSize()) {
      // List pane
      Box(modifier = Modifier.fillMaxHeight().weight(0.4f)) {
        DashboardScreen(
          onNavigateToProductDetail = { productId ->
            selectedProductId = productId
          }
        )
      }
      
      // Detail pane
      Box(modifier = Modifier.fillMaxHeight().weight(0.6f)) {
        selectedProductId?.let { productId ->
          ProductDetailScreen(
            productId = productId,
            onBack = {
              selectedProductId = null
            }
          )
        } ?: DashboardPlaceholder()
      }
    }
  } else {
    // Single-pane layout for compact screens - use navigation
    DashboardScreen(
      onNavigateToProductDetail = onNavigateToProductDetail
    )
  }
}

@Composable
private fun DashboardPlaceholder() {
  Box(modifier = Modifier.fillMaxSize()) {
    // Empty placeholder when no product is selected
  }
}
