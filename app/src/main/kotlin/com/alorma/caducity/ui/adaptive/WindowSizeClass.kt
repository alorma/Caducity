package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject

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
 * Returns true only if tablet mode is enabled AND window size is expanded.
 */
@Composable
fun rememberIsExpanded(
  tabletModeRemoteConfig: TabletModeRemoteConfig = koinInject(),
): Boolean {
  val windowSizeClass = LocalWindowSizeClass.current
  return remember(windowSizeClass, tabletModeRemoteConfig) {
    tabletModeRemoteConfig.isEnabled() && windowSizeClass.isExpanded()
  }
}

/**
 * Composable helper to check if window is expanded or medium (tablet+).
 * Returns true only if tablet mode is enabled AND window size is expanded or medium.
 */
@Composable
fun rememberIsExpandedOrMedium(
  tabletModeRemoteConfig: TabletModeRemoteConfig = koinInject(),
): Boolean {
  val windowSizeClass = LocalWindowSizeClass.current
  return remember(windowSizeClass, tabletModeRemoteConfig) {
    tabletModeRemoteConfig.isEnabled() && windowSizeClass.isExpandedOrMedium()
  }
}

/**
 * Composable helper to check if window is compact (phone).
 * Always returns the actual window size check (not affected by tablet mode flag).
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
 * Returns tablet column count (5/7) only if tablet mode is enabled, otherwise defaults to 3.
 */
@Composable
fun rememberGridColumns(
  tabletModeRemoteConfig: TabletModeRemoteConfig = koinInject(),
): Int {
  val windowSizeClass = LocalWindowSizeClass.current
  return remember(windowSizeClass, tabletModeRemoteConfig) {
    if (tabletModeRemoteConfig.isEnabled()) {
      windowSizeClass.calculateGridColumns()
    } else {
      3 // Always use phone layout (3 columns) when tablet mode is disabled
    }
  }
}
