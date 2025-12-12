package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

@Composable
fun isWidthCompact(): Boolean {
  return currentWindowAdaptiveInfo(
    supportLargeAndXLargeWidth = true,
  ).isCompact
}

@Composable
fun isWidthMedium(): Boolean {
  return currentWindowAdaptiveInfo(
    supportLargeAndXLargeWidth = true,
  ).isMedium
}

private val WindowSizeClass.isCompact: Boolean
  get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)

private val WindowAdaptiveInfo.isCompact: Boolean
  get() = !windowSizeClass.isCompact

private val WindowSizeClass.isMedium: Boolean
  get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

private val WindowAdaptiveInfo.isMedium: Boolean
  get() = windowSizeClass.isMedium
