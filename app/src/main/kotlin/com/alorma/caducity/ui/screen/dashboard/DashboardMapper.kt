package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime

class DashboardMapper(
  private val appClock: AppClock,
  private val dateFormat: DateTimeFormat<LocalDate>,
  private val localizedDateFormatter: LocalizedDateFormatter,
) {

  fun mapToDashboardSections(
    instances: ImmutableList<ProductInstance>,
  ): DashboardState {
    val summary = calculateSummary(instances)

    val startDate = instances.minBy { instance ->
      instance.expirationDate
    }.expirationDate

    val endDate = instances.maxBy { instance ->
      instance.expirationDate
    }.expirationDate

    val calendarState = CalendarState(
      today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
      startLocalDate = startDate.toLocalDateTime(TimeZone.currentSystemDefault()).date,
      endLocalDate = endDate.toLocalDateTime(TimeZone.currentSystemDefault()).date,
      content = CalendarData(persistentMapOf()),
      monthNames = localizedDateFormatter.getMonthNames(),
      daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
    )

    return DashboardState.Success(
      summary = summary,
      calendarState = calendarState,
    )
  }

  private fun calculateSummary(instances: List<ProductInstance>): DashboardSummary {
    val expiredCount = instances
      .filter { instance ->
        instance.status is InstanceStatus.Expired
      }.size
    val expiringSoonCount = instances
      .filter { instance ->
        instance.status is InstanceStatus.ExpiringSoon
      }.size
    val freshCount = instances
      .filter { instance ->
        instance.status is InstanceStatus.Fresh
      }.size
    val frozenCount = getStatusCount(instances, InstanceStatus.Frozen)

    return DashboardSummary(
      expired = expiredCount,
      expiringSoon = expiringSoonCount,
      fresh = freshCount,
      frozen = frozenCount,
    )
  }

  private fun getStatusCount(
    instances: List<ProductInstance>,
    status: InstanceStatus.Frozen,
  ): Int {
    return instances
      .filter { instance -> instance.status == status }
      .size
  }
  /*
    private fun calculateCalendarState(
      products: List<ProductUiModel>,
    ): CalendarState {
      val today = appClock.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

      // Calculate start and end months for calendar range
      val currentMonth = YearMonth(today.year, today.month)
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

      // Group products by date with most critical status
      val productsByDate = buildMap {
        products.forEach { product ->
          if (product is ProductUiModel.WithInstances) {
            product.instances.forEach { instance ->
              // Use expirationDate here as UI model already converted to correct display date
              // For frozen items: expirationDate field already contains the pausedDate
              // For normal items: expirationDate contains the expiration date
              val date = instance.expirationDate
              val currentStatus = get(date)

              // Keep the most critical status (Expired > ExpiringSoon > Frozen > Fresh)
              val newStatus = when {
                currentStatus == InstanceStatus.Expired -> InstanceStatus.Expired
                instance.status == InstanceStatus.Expired -> InstanceStatus.Expired
                currentStatus == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
                instance.status == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
                currentStatus == InstanceStatus.Frozen -> InstanceStatus.Frozen
                instance.status == InstanceStatus.Frozen -> InstanceStatus.Frozen
                else -> InstanceStatus.Fresh
              }

              put(date, newStatus)
            }
          }
        }

        // Ensure today is always in the map, even if no products
        if (!containsKey(today)) {
          put(today, null)
        }
      }

      // Calculate shape position for each date
      val dateWithShapes = productsByDate.mapValues { (date, status) ->
        val hasPrevDay = productsByDate.containsKey(date.plus(-1, DateTimeUnit.DAY))
        val hasNextDay = productsByDate.containsKey(date.plus(1, DateTimeUnit.DAY))

        val shapePosition = when {
          date == today && !hasPrevDay && !hasNextDay -> ShapePosition.Single
          date == today && hasPrevDay && !hasNextDay -> ShapePosition.End
          !hasPrevDay && !hasNextDay -> ShapePosition.Single
          !hasPrevDay && hasNextDay -> ShapePosition.Start
          hasPrevDay && !hasNextDay -> ShapePosition.End
          else -> ShapePosition.Middle
        }

        CalendarDateInfo(status, shapePosition)
      }.toImmutableMap()

      val calendarData = CalendarData(dateWithShapes)

      return CalendarState(
        today = today,
        content = calendarData,
        monthNames = localizedDateFormatter.getMonthNames(),
        daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
      )
    }
  */
}
