package com.alorma.caducity.ui.components.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class CalendarScreenshotTest {

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun CaducityMonthCalendarTest(
    @PreviewParameter(provider = CalendarStateProvider::class) calendarState: CalendarState,
  ) {
    CaducityMonthCalendarPreview(calendarState)
  }

}