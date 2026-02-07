package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

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
