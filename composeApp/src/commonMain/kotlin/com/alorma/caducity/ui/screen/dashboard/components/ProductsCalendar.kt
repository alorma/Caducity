package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.components.shape.ShapePosition
import com.alorma.caducity.base.ui.components.shape.toCalendarShape
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.time.clock.AppClock
import com.alorma.caducity.ui.screen.dashboard.CalendarData
import com.alorma.caducity.ui.screen.dashboard.ExpirationDefaults
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun ProductsCalendar(
  calendarData: CalendarData,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
  appClock: AppClock = koinInject(),
) {
  val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
  val currentMonth = YearMonth(today.year, today.month)

  // Calculate start and end months
  val currentMonthNum = today.month.ordinal + 1 // Month ordinal is 0-based
  val startMonthNum = currentMonthNum - 1
  val startMonth = if (startMonthNum >= 1) {
    YearMonth(today.year, startMonthNum)
  } else {
    YearMonth(today.year - 1, 12)
  }

  val endMonthNum = currentMonthNum + 3
  val endMonth = if (endMonthNum <= 12) {
    YearMonth(today.year, endMonthNum)
  } else {
    YearMonth(today.year + 1, endMonthNum - 12)
  }
  val calendarState = rememberCalendarState(
    startMonth = startMonth,
    endMonth = endMonth,
    firstVisibleMonth = currentMonth,
  )

  val weekCalendarState = rememberWeekCalendarState(
    startDate = startMonth.firstDay,
    endDate = endMonth.lastDay,
    firstDayOfWeek = appClock.now()
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .dayOfWeek,
  )

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {

    WeekCalendar(
      state = weekCalendarState,
      contentPadding = PaddingValues(horizontal = 16.dp),
      weekHeader = { week ->
        val daysOfWeek = remember {
          week.days.map { weekDay ->
            weekDay.date.dayOfWeek
          }.toImmutableList()
        }

        val firstMonth = week.days.first().date.month
        val lastMonth = week.days.last().date.month
        val firstYear = week.days.first().date.year
        val lastYear = week.days.last().date.year

        CalendarHeader(
          firstMonth = firstMonth,
          secondMonth = lastMonth,
          daysOfWeek = daysOfWeek,
          highlightToday = true,
          firstYear = firstYear,
          secondYear = lastYear,
        )
      },
      dayContent = { weekDay ->
        DayContentWrapper(
          date = weekDay.date,
          calendarData = calendarData,
          onDateClick = onDateClick,
        )
      },
    )

    HorizontalCalendar(
      modifier = Modifier.fillMaxWidth(),
      state = calendarState,
      contentPadding = PaddingValues(horizontal = 16.dp),
      monthHeader = { calendarMonth ->
        val daysOfWeek = remember {
          calendarMonth.weekDays.first().map { weekDay ->
            weekDay.date.dayOfWeek
          }.toImmutableList()
        }

        val firstMonth = calendarMonth.yearMonth.month
        val lastMonth = calendarMonth.weekDays.last().last().date.month
        val firstYear = calendarMonth.weekDays.first().first().date.year
        val lastYear = calendarMonth.weekDays.last().last().date.year

        CalendarHeader(
          firstMonth = firstMonth,
          secondMonth = lastMonth,
          daysOfWeek = daysOfWeek,
          firstYear = firstYear,
          secondYear = lastYear,
        )
      },
      dayContent = { calendarDay ->
        DayContentWrapper(
          date = calendarDay.date,
          calendarData = calendarData,
          onDateClick = onDateClick,
        )
      },
    )
  }
}

@Composable
private fun CalendarHeader(
  firstMonth: Month,
  secondMonth: Month,
  daysOfWeek: ImmutableList<DayOfWeek>,
  modifier: Modifier = Modifier,
  highlightToday: Boolean = false,
  firstYear: Int? = null,
  secondYear: Int? = null,
  dateFormatter: LocalizedDateFormatter = koinInject(),
  appClock: AppClock = koinInject(),
) {
  val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
  val todayDayOfWeek = today.dayOfWeek

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
          if (secondYear != null && secondYear != firstYear) {
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
      daysOfWeek.forEach { dayOfWeek ->
        val isToday = highlightToday && todayDayOfWeek == dayOfWeek

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

@Composable
private fun DayContentWrapper(
  date: LocalDate,
  calendarData: CalendarData,
  onDateClick: (LocalDate) -> Unit
) {
  val kotlinDate = LocalDate(date.year, date.month, date.day)
  val dateInfo = calendarData.productsByDate[kotlinDate]

  DayContent(
    date = date,
    status = dateInfo?.status,
    shapePosition = dateInfo?.shapePosition ?: ShapePosition.None,
    onClick = { onDateClick(it) },
  )
}

@Composable
private fun DayContent(
  date: LocalDate,
  status: InstanceStatus?,
  shapePosition: ShapePosition,
  onClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {

  val backgroundColor = if (status != null) {
    ExpirationDefaults.getColors(status).container
  } else {
    Color.Transparent
  }

  val textColor = if (status != null) {
    ExpirationDefaults.getColors(status).onContainer
  } else {
    CaducityTheme.colorScheme.onSurface
  }

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .padding(2.dp)
      .clip(shapePosition.toCalendarShape())
      .background(backgroundColor)
      .clickable { onClick(date) },
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = date.day.toString(),
      style = CaducityTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      color = textColor,
      fontWeight = if (status != null) FontWeight.Bold else FontWeight.Normal,
    )
  }
}

@Preview
@Composable
private fun ProductsCalendarPreview() {
  AppPreview {
    // Create empty calendar data for preview
    val emptyCalendarData = CalendarData(
      productsByDate = kotlinx.collections.immutable.persistentMapOf()
    )
    ProductsCalendar(
      calendarData = emptyCalendarData,
      onDateClick = {},
    )
  }
}
