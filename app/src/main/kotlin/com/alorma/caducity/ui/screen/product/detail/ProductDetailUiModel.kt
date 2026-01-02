package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.dashboard.CalendarData
import com.alorma.caducity.ui.screen.dashboard.CalendarDateInfo
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.time.Instant

@Stable
data class ProductDetailUiModel(
  val id: String,
  val name: String,
  val description: String,
  val instances: List<ProductInstanceDetailUiModel>,
)

@Stable
data class ProductInstanceDetailUiModel(
  val id: String,
  val identifier: String,
  val status: InstanceStatus,
  val expirationDate: LocalDate,
  val expirationDateText: String,
  val expirationInstant: Instant,
)

/**
 * Convert product instances to calendar state for visualization
 * @param today Current date for calendar calculations
 * @param hideConsumed If true, filters out consumed instances from the calendar
 */
fun List<ProductInstanceDetailUiModel>.toCalendarState(
  today: LocalDate,
  hideConsumed: Boolean = true
): CalendarState {
  val filteredInstances = if (hideConsumed) {
    this.filter { it.status != InstanceStatus.Consumed }
  } else {
    this
  }

  // Calculate start and end months for calendar range
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

  // Group instances by expiration date
  val groupedByDate = filteredInstances.groupBy { it.expirationDate }

  // Convert to CalendarDateInfo with shape positions for consecutive dates
  val sortedDates = groupedByDate.keys.sorted()
  val productsByDate = sortedDates.associateWith { date ->
    val instancesOnDate = groupedByDate[date]!!

    // Determine the "worst" status for this date (priority: Expired > ExpiringSoon > Fresh > Frozen)
    val status = instancesOnDate.maxByOrNull { instance ->
      when (instance.status) {
        InstanceStatus.Expired -> 3
        InstanceStatus.ExpiringSoon -> 2
        InstanceStatus.Fresh -> 1
        InstanceStatus.Frozen -> 0
        InstanceStatus.Consumed -> -1 // Should already be filtered out
      }
    }?.status ?: InstanceStatus.Fresh

    // Calculate shape position for visual grouping
    val dateIndex = sortedDates.indexOf(date)
    val hasPrevious = dateIndex > 0 && sortedDates[dateIndex - 1] == date.minusDays(1)
    val hasNext = dateIndex < sortedDates.size - 1 && sortedDates[dateIndex + 1] == date.plusDays(1)

    val shapePosition = when {
      hasPrevious && hasNext -> ShapePosition.Middle
      hasPrevious -> ShapePosition.End
      hasNext -> ShapePosition.Start
      else -> ShapePosition.None
    }

    CalendarDateInfo(
      status = status,
      shapePosition = shapePosition
    )
  }

  val calendarData = CalendarData(productsByDate = productsByDate.toImmutableMap())

  return CalendarState(
    startMonth = startMonth,
    endMonth = endMonth,
    today = today,
    calendarData = calendarData,
  )
}

private fun LocalDate.minusDays(days: Int): LocalDate {
  return LocalDate(year, month, day).let { date ->
    // Simple implementation - convert to epoch days and subtract
    val epochDays = date.toEpochDays() - days
    LocalDate.fromEpochDays(epochDays)
  }
}

private fun LocalDate.plusDays(days: Int): LocalDate {
  return LocalDate(year, month, day).let { date ->
    // Simple implementation - convert to epoch days and add
    val epochDays = date.toEpochDays() + days
    LocalDate.fromEpochDays(epochDays)
  }
}
