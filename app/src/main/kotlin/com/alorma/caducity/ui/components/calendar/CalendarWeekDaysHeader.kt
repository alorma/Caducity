package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
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
      Text(text = dayNameFromState(weekDay, dayOfWeekNames))
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