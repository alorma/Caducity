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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun CalendarHeader(
  firstMonth: Month,
  secondMonth: Month,
  daysOfWeek: ImmutableList<DayOfWeek>,
  modifier: Modifier = Modifier,
  weekDates: ImmutableList<LocalDate>? = null,
  highlightToday: Boolean = false,
  firstYear: Int? = null,
  secondYear: Int? = null,
  dateFormatter: LocalizedDateFormatter = koinInject(),
  appClock: AppClock = koinInject(),
) {
  val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

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
        if (firstYear != null) {
          Text(
            text = firstYear.toString(),
            style = CaducityTheme.typography.labelLarge,
            color = CaducityTheme.colorScheme.onSurface.copy(
              alpha = CaducityTheme.dims.dim2,
            ),
          )
        }
        Text(
          text = dateFormatter.getMonthName(firstMonth),
          style = CaducityTheme.typography.titleMedium,
          color = CaducityTheme.colorScheme.onSurface.copy(
            alpha = CaducityTheme.dims.dim1,
          ),
        )
      }

      if (secondMonth != firstMonth) {
        Column(horizontalAlignment = Alignment.End) {
          if (secondYear != null) {
            Text(
              text = secondYear.toString(),
              style = CaducityTheme.typography.labelLarge,
              color = CaducityTheme.colorScheme.onSurface.copy(
                alpha = CaducityTheme.dims.dim2,
              ),
            )
          }
          Text(
            text = dateFormatter.getMonthName(secondMonth),
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
      daysOfWeek.forEachIndexed { index, dayOfWeek ->
        // Check if today is actually in this week by comparing dates, not just day names
        val isToday = if (highlightToday && weekDates != null) {
          weekDates.getOrNull(index) == today
        } else {
          false
        }

        Text(
          text = dateFormatter.getDayOfWeekAbbreviation(dayOfWeek),
          style = CaducityTheme.typography.labelSmall,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center,
          color = if (isToday) {
            CaducityTheme.colorScheme.primary
          } else {
            CaducityTheme.colorScheme.onSurface
          },
          fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
        )
      }
    }
  }
}