package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowSizeClass

/**
 * Utility to determine if the current window is considered "expanded" (tablet/desktop).
 * Uses Material 3 window size class definitions:
 * - Compact: <600dp (phones)
 * - Medium: 600-840dp (small tablets, unfolded foldables)
 * - Expanded: >840dp (large tablets, desktops)
 */
fun WindowSizeClass.isExpanded(): Boolean = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

/**
 * Composable helper to check if window is expanded (tablet/desktop).
 */
@Composable
fun rememberIsExpanded(): Boolean {
  val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
  return remember(windowSizeClass) {
    windowSizeClass.isExpanded()
  }
}
