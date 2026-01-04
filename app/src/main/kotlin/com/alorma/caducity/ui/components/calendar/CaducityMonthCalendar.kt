package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.dashboard.CalendarData
import com.alorma.caducity.ui.screen.dashboard.CalendarDateInfo
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import com.alorma.caducity.ui.screen.dashboard.DaysOfWeekNames
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.kizitonwose.calendar.compose.CalendarItemInfo
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.minusDays
import com.kizitonwose.calendar.core.plusDays
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.yearMonth
import kotlin.time.Duration.Companion.days

@Composable
fun CaducityMonthCalendar(
  calendarState: CalendarState,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val monthCalendarState = rememberCalendarState(
    startMonth = calendarState.startMonth,
    endMonth = calendarState.endMonth,
    firstVisibleMonth = calendarState.today.yearMonth,
  )

  HorizontalCalendar(
    modifier = modifier.fillMaxWidth(),
    state = monthCalendarState,
    contentPadding = PaddingValues(horizontal = 16.dp),
    monthHeader = { calendarMonth ->
      val daysOfWeek = remember {
        calendarMonth.weekDays.first().map { weekDay ->
          weekDay.date.dayOfWeek
        }.toImmutableList()
      }

      CalendarHeader(
        calendarState = calendarState,
        daysOfWeek = daysOfWeek,
      )
    },
    dayContent = { calendarDay ->
      val date = calendarDay.date
      val dateInfo = calendarState.calendarData.productsByDate[date]

      DayContent(
        today = calendarState.today,
        date = date,
        status = dateInfo?.status,
        shapePosition = dateInfo?.shapePosition ?: ShapePosition.None,
        onClick = onDateClick,
        isOutDay = calendarDay.position != DayPosition.MonthDate,
      )
    },
  )
}

private val daysOfWeekNames = DaysOfWeekNames(
  monday = "mon",
  tuesday = "tue",
  wednesday = "wed",
  thursday = "thu",
  friday = "fri",
  saturday = "sat",
  sunday = "sun",
)

private val today = LocalDate(2026, 2, 11)

class CalendarStateProvider : CollectionPreviewParameterProvider<CalendarState>(
  listOf(
    CalendarState(
      startMonth = YearMonth(2026, 2),
      startMonthName = "February",
      endMonth = YearMonth(2026, 3),
      endMonthName = "March",
      today = today,
      calendarData = CalendarData(
        productsByDate = emptyMap<LocalDate, CalendarDateInfo>().toImmutableMap(),
      ),
      daysOfWeekNames = daysOfWeekNames,
    ),
    CalendarState(
      startMonth = YearMonth(2026, 2),
      startMonthName = "February",
      endMonth = YearMonth(2026, 3),
      endMonthName = "March",
      today = today,
      calendarData = CalendarData(
        productsByDate = mapOf(
          today.minusDays(1) to CalendarDateInfo(
            status = InstanceStatus.Expired, shapePosition = ShapePosition.Start,
          ),
          today to CalendarDateInfo(
            status = InstanceStatus.ExpiringSoon, shapePosition = ShapePosition.Middle,
          ),
          today.plusDays(1) to CalendarDateInfo(
            status = InstanceStatus.Frozen, shapePosition = ShapePosition.Middle,
          ),
          today.plusDays(2) to CalendarDateInfo(
            status = InstanceStatus.Fresh, shapePosition = ShapePosition.End,
          ),
          today.plusDays(6) to CalendarDateInfo(
            status = InstanceStatus.Fresh, shapePosition = ShapePosition.Single,
          ),
        ).toImmutableMap(),
      ),
      daysOfWeekNames = daysOfWeekNames,
    ),
  ),
)

@PreviewDynamicLightDark
@Composable
fun CaducityMonthCalendarPreview(
  @PreviewParameter(provider = CalendarStateProvider::class) calendarState: CalendarState,
) {
  PreviewTheme {
    Surface {
      CaducityMonthCalendar(
        calendarState = calendarState,
        onDateClick = {},
      )
    }
  }
}