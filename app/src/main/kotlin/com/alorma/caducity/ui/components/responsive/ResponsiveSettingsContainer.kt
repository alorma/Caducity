package com.alorma.caducity.ui.components.responsive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.adaptive.rememberIsExpanded

/**
 * Responsive container for settings screens.
 *
 * On tablets (≥840dp):
 * - Content is centered on screen
 * - Maximum width: 600dp
 * - Horizontal padding maintained by screen content (16dp)
 *
 * On phones (<840dp):
 * - Full-width layout
 * - Existing 16dp horizontal padding maintained by screen content
 */
@Composable
fun ResponsiveSettingsContainer(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val isExpanded = rememberIsExpanded()

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Box(
      modifier = if (isExpanded) {
        Modifier.fillMaxWidth().widthIn(max = 600.dp)
      } else {
        Modifier.fillMaxWidth()
      }
    ) {
      content()
    }
  }
}
