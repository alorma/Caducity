package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames

@Composable
fun CalendarWeekDaysHeader(
  weekDays: ImmutableList<LocalDate>,
  dayOfWeekNames: DayOfWeekNames,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(12.dp)
      .then(modifier),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    weekDays.forEach { weekDay ->
      Text(
        text = dayNameFromState(weekDay, dayOfWeekNames),
        style = CaducityTheme.typography.labelSmall,
      )
    }
  }
}

@Composable
private fun dayNameFromState(
  date: LocalDate,
  dayOfWeekNames: DayOfWeekNames,
): String {
  return LocalDate.Format {
    dayOfWeek(dayOfWeekNames)
  }.format(date)
}

@PreviewLightDark
@Composable
private fun CalendarWeekDaysHeaderPreview() {
  PreviewTheme {
    Surface {
      CalendarWeekDaysHeader(
        weekDays = persistentListOf(
          LocalDate(2026, 1, 5),  // Monday
          LocalDate(2026, 1, 6),  // Tuesday
          LocalDate(2026, 1, 7),  // Wednesday
          LocalDate(2026, 1, 8),  // Thursday
          LocalDate(2026, 1, 9),  // Friday
          LocalDate(2026, 1, 10), // Saturday
          LocalDate(2026, 1, 11), // Sunday
        ),
        dayOfWeekNames = DayOfWeekNames.ENGLISH_ABBREVIATED,
      )
    }
  }
}
