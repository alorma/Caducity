package com.alorma.caducity.ui.components.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class CalendarScreenshotTest {

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun CaducityWeekCalendarTest(
    @PreviewParameter(provider = CalendarStateProvider::class) appCalendarConfig: AppCalendarConfig,
  ) {
    CaducityWeekCalendarPreview(appCalendarConfig)
  }

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun CaducityMonthCalendarTest(
    @PreviewParameter(provider = CalendarStateProvider::class) appCalendarConfig: AppCalendarConfig,
  ) {
    CaducityMonthCalendarPreview(appCalendarConfig)
  }

}