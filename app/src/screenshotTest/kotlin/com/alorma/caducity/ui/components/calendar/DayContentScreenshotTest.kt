package com.alorma.caducity.ui.components.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class DayContentScreenshotTest {

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun DayTodayWithStatusContentPreviewTheme() {
    DayTodayWithStatusScreenshot()
  }

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun DayTodayWithoutStatusContentPreviewTheme() {
    DayTodayWithoutStatusScreenshot()
  }

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun DayWithStatusContentPreviewTheme() {
    DayWithStatusScreenshot()
  }

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun DayWithoutStatusContentPreviewTheme() {
    DayWithoutStatusScreenshot()
  }
}
