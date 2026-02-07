package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Utility to determine if the current window is considered "expanded" (tablet/desktop).
 * Uses Material 3 window size class definitions:
 * - Compact: <600dp (phones)
 * - Medium: 600-840dp (small tablets, unfolded foldables)
 * - Expanded: >840dp (large tablets, desktops)
 */
fun WindowSizeClass.isExpanded(): Boolean {
  return widthSizeClass == WindowWidthSizeClass.Expanded
}

fun WindowSizeClass.isExpandedOrMedium(): Boolean {
  return widthSizeClass == WindowWidthSizeClass.Expanded ||
    widthSizeClass == WindowWidthSizeClass.Medium
}

fun WindowSizeClass.isCompact(): Boolean {
  return widthSizeClass == WindowWidthSizeClass.Compact
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
  return when (widthSizeClass) {
    WindowWidthSizeClass.Compact -> 3
    WindowWidthSizeClass.Medium -> 5
    WindowWidthSizeClass.Expanded -> 7
    else -> 3
  }
}

// Composable helpers for feature flag integration

/**
 * Composable helper to check if window is expanded (tablet/desktop).
 * Can be enhanced with feature flag checks.
 */
@Composable
fun rememberIsExpanded(): Boolean {
  val windowSizeClass = LocalWindowSizeClass.current
  return remember(windowSizeClass) {
    windowSizeClass.isExpanded()
  }
}

/**
 * Composable helper to check if window is expanded or medium (tablet+).
 * Can be enhanced with feature flag checks.
 */
@Composable
fun rememberIsExpandedOrMedium(): Boolean {
  val windowSizeClass = LocalWindowSizeClass.current
  return remember(windowSizeClass) {
    windowSizeClass.isExpandedOrMedium()
  }
}

/**
 * Composable helper to check if window is compact (phone).
 * Can be enhanced with feature flag checks.
 */
@Composable
fun rememberIsCompact(): Boolean {
  val windowSizeClass = LocalWindowSizeClass.current
  return remember(windowSizeClass) {
    windowSizeClass.isCompact()
  }
}

/**
 * Composable helper to calculate responsive grid columns.
 * Can be enhanced with feature flag checks to override behavior.
 */
@Composable
fun rememberGridColumns(): Int {
  val windowSizeClass = LocalWindowSizeClass.current
  return remember(windowSizeClass) {
    windowSizeClass.calculateGridColumns()
  }
}
