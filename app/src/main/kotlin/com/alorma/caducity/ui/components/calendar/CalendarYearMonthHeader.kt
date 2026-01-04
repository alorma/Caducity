package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.MonthNames

@Composable
fun CalendarYearMonthHeader(
  startYearMonth: YearMonth,
  endYearMonth: YearMonth,
  monthNames: MonthNames,
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
          text = startYearMonth.year.toString(),
          style = CaducityTheme.typography.labelLarge,
          color = CaducityTheme.colorScheme.onSurface.copy(
            alpha = CaducityTheme.dims.dim2,
          ),
        )
        Text(
          text = monthNameFromState(startYearMonth, monthNames),
          style = CaducityTheme.typography.titleMedium,
          color = CaducityTheme.colorScheme.onSurface.copy(
            alpha = CaducityTheme.dims.dim1,
          ),
        )
      }
      if (endYearMonth != startYearMonth) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = endYearMonth.year.toString(),
            style = CaducityTheme.typography.labelLarge,
            color = CaducityTheme.colorScheme.onSurface.copy(
              alpha = CaducityTheme.dims.dim2,
            ),
          )
          Text(
            text = monthNameFromState(endYearMonth, monthNames),
            style = CaducityTheme.typography.titleMedium,
            color = CaducityTheme.colorScheme.onSurface.copy(
              alpha = CaducityTheme.dims.dim1,
            ),
          )
        }
      }
    }
  }
}

@Composable
private fun monthNameFromState(
  yearMonth: YearMonth,
  monthNames: MonthNames,
): String {
  return LocalDate.Format {
    monthName(monthNames)
  }.format(yearMonth.firstDay)
}

@PreviewLightDark
@Composable
private fun CalendarYearMonthHeaderPreview() {
  PreviewTheme {
    Surface {
      CalendarYearMonthHeader(
        startYearMonth = YearMonth(2026, Month.JANUARY),
        endYearMonth = YearMonth(2026, Month.JANUARY),
        monthNames = MonthNames.ENGLISH_FULL,
      )
    }
  }
}

@PreviewLightDark
@Composable
private fun CalendarYearMonthHeaderTwoMonthsPreview() {
  PreviewTheme {
    Surface {
      CalendarYearMonthHeader(
        startYearMonth = YearMonth(2025, Month.DECEMBER),
        endYearMonth = YearMonth(2026, Month.JANUARY),
        monthNames = MonthNames.ENGLISH_FULL,
      )
    }
  }
}