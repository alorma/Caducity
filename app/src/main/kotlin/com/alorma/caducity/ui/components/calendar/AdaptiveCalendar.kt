package com.alorma.caducity.ui.components.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.adaptive.rememberIsExpandedOrMedium
import kotlinx.datetime.LocalDate

/**
 * Adaptive calendar that switches between week and month views based on screen size:
 * - Compact (<600dp): Week view
 * - Medium/Expanded (≥600dp): Month view
 */
@Composable
fun AdaptiveCalendar(
  appCalendarConfig: AppCalendarConfig,
  todayColor: Color,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val shouldShowMonth = rememberIsExpandedOrMedium()

  if (shouldShowMonth) {
    CaducityMonthCalendar(
      appCalendarConfig = appCalendarConfig,
      onDateClick = onDateClick,
      modifier = modifier,
    )
  } else {
    CaducityWeekCalendar(
      appCalendarConfig = appCalendarConfig,
      todayColor = todayColor,
      onDateClick = onDateClick,
      modifier = modifier,
    )
  }
}
