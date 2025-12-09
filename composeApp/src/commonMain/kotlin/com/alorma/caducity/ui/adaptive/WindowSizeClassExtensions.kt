package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

@Composable
fun isWidthCompact(): Boolean {
  return currentWindowAdaptiveInfo(
    supportLargeAndXLargeWidth = true,
  ).isWidthCompact
}

@Composable
fun isLarge(): Boolean {
  return currentWindowAdaptiveInfo(
    supportLargeAndXLargeWidth = true,
  ).isLarge
}

private val WindowSizeClass.isWidthMediumOrLarger: Boolean
  get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

private val WindowSizeClass.isLarge: Boolean
  get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)

private val WindowSizeClass.isWidthCompact: Boolean
  get() = !isWidthMediumOrLarger

private val WindowAdaptiveInfo.isWidthCompact: Boolean
  get() = windowSizeClass.isWidthCompact

private val WindowAdaptiveInfo.isWidthMediumOrLarger: Boolean
  get() = windowSizeClass.isWidthMediumOrLarger

private val WindowAdaptiveInfo.isLarge: Boolean
  get() = windowSizeClass.isLarge

