package com.alorma.caducity.ui.components.responsive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.adaptive.rememberIsExpanded

/**
 * Responsive centered container for tablet-optimized layouts.
 *
 * On tablets (≥840dp):
 * - Content is centered on screen
 * - Maximum width constraint applied (configurable)
 * - Horizontal padding maintained by screen content
 *
 * On phones (<840dp):
 * - Full-width layout
 * - No width constraints applied
 *
 * @param maxWidth Maximum width for centered content on tablets (default: 600dp for forms/settings)
 * @param modifier Modifier to be applied to the container
 * @param fillHeight Whether to fill maximum height (default: true). Set to false if content should wrap its height.
 * @param content Content to be displayed
 */
@Composable
fun ResponsiveCenteredContainer(
  maxWidth: Dp = 600.dp,
  modifier: Modifier = Modifier,
  fillHeight: Boolean = true,
  content: @Composable () -> Unit,
) {
  val isExpanded = rememberIsExpanded()

  if (isExpanded) {
    Box(
      modifier =
        if (fillHeight) {
          modifier.fillMaxSize()
        } else {
          modifier.fillMaxWidth()
        },
      contentAlignment = Alignment.TopCenter,
    ) {
      Box(
        modifier =
          Modifier
            .widthIn(max = maxWidth)
            .fillMaxWidth(),
      ) {
        content()
      }
    }
  } else {
    Box(
      modifier =
        if (fillHeight) {
          modifier.fillMaxSize()
        } else {
          modifier.fillMaxWidth()
        },
    ) {
      content()
    }
  }
}

/**
 * Responsive container for settings screens.
 * Uses 600dp max width - optimal for form-like content.
 *
 * @see ResponsiveCenteredContainer
 */
@Composable
fun ResponsiveSettingsContainer(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  ResponsiveCenteredContainer(
    maxWidth = 600.dp,
    modifier = modifier,
    content = content,
  )
}
