package com.alorma.caducity.ui.components.calendar


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.DayOfWeek

@Composable
fun CalendarHeader(
  calendarState: CalendarState,
  daysOfWeek: ImmutableList<DayOfWeek>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 24.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(horizontalAlignment = Alignment.Start) {
        Text(
          text = calendarState.startMonth.year.toString(),
          style = CaducityTheme.typography.labelLarge,
          color = CaducityTheme.colorScheme.onSurface.copy(
            alpha = CaducityTheme.dims.dim2,
          ),
        )
        Text(
          text = calendarState.startMonthName,
          style = CaducityTheme.typography.titleMedium,
          color = CaducityTheme.colorScheme.onSurface.copy(
            alpha = CaducityTheme.dims.dim1,
          ),
        )
      }

      if (calendarState.endMonth != calendarState.startMonth) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = calendarState.endMonth.year.toString(),
            style = CaducityTheme.typography.labelLarge,
            color = CaducityTheme.colorScheme.onSurface.copy(
              alpha = CaducityTheme.dims.dim2,
            ),
          )
          Text(
            text = calendarState.endMonthName,
            style = CaducityTheme.typography.titleMedium,
            color = CaducityTheme.colorScheme.onSurface.copy(
              alpha = CaducityTheme.dims.dim1,
            ),
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      daysOfWeek.forEach { dayOfWeek: DayOfWeek ->
        val dayName = when (dayOfWeek) {
          DayOfWeek.MONDAY -> calendarState.daysOfWeekNames.monday
          DayOfWeek.TUESDAY -> calendarState.daysOfWeekNames.tuesday
          DayOfWeek.WEDNESDAY -> calendarState.daysOfWeekNames.wednesday
          DayOfWeek.THURSDAY -> calendarState.daysOfWeekNames.thursday
          DayOfWeek.FRIDAY -> calendarState.daysOfWeekNames.friday
          DayOfWeek.SATURDAY -> calendarState.daysOfWeekNames.saturday
          DayOfWeek.SUNDAY -> calendarState.daysOfWeekNames.sunday
        }

        Text(
          text = dayName,
          style = CaducityTheme.typography.labelSmall,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}