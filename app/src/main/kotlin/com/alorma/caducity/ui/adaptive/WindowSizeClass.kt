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
fun WindowSizeClass.isExpanded(): Boolean {
  return isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
}

fun WindowSizeClass.isExpandedOrMedium(): Boolean {
  return isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
}

fun WindowSizeClass.isCompact(): Boolean {
  return !isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
}

/**
 * Calculate responsive column count for item grids based on available width.
 *
 * Breakpoints:
 * - <600dp (Compact): 3 columns
 * - 600-840dp (Medium): 5 columns
 * - >840dp (Expanded): 7 columns
 */
fun WindowSizeClass.calculateGridColumns(): Int {
  return when {
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 7
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 5
    else -> 3
  }
}

// Composable helpers for feature flag integration

/**
 * Composable helper to check if window is expanded (tablet/desktop).
 * Returns true only if tablet mode is enabled AND window size is expanded.
 */
@Composable
fun rememberIsExpanded(): Boolean {
  val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
  return remember(windowSizeClass) {
    windowSizeClass.isExpanded()
  }
}

/**
 * Composable helper to check if window is expanded or medium (tablet+).
 * Returns true only if tablet mode is enabled AND window size is expanded or medium.
 */
@Composable
fun rememberIsExpandedOrMedium(): Boolean {
  val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
  return remember(windowSizeClass) {
    windowSizeClass.isExpandedOrMedium()
  }
}

/**
 * Composable helper to check if window is compact (phone).
 * Always returns the actual window size check (not affected by tablet mode flag).
 */
@Composable
fun rememberIsCompact(): Boolean {
  val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
  return remember(windowSizeClass) {
    windowSizeClass.isCompact()
  }
}

/**
 * Composable helper to calculate responsive grid columns.
 * Returns tablet column count (5/7) only if tablet mode is enabled, otherwise defaults to 3.
 */
@Composable
fun rememberGridColumns(): Int {
  val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
  return remember(windowSizeClass) {
    windowSizeClass.calculateGridColumns()
  }
}
