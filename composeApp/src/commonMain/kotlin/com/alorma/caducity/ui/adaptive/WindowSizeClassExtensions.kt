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

private val WindowSizeClass.isCompact: Boolean
  get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)

private val WindowAdaptiveInfo.isCompact: Boolean
  get() = !windowSizeClass.isCompact
