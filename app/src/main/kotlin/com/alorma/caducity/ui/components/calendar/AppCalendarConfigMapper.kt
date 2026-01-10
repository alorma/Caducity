package com.alorma.caducity.ui.components.calendar

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class AppCalendarConfigMapper(
  private val appClock: AppClock,
  private val localizedDateFormatter: LocalizedDateFormatter,
) {

  fun createFromInstances(
    instances: List<ProductInstance>,
  ): AppCalendarConfig {
    val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    return if (instances.isEmpty()) {
      createEmpty(today)
    } else {
      createWithInstances(instances)
    }
  }

  fun createEmpty(
    today: LocalDate = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
  ): AppCalendarConfig {
    return AppCalendarConfig(
      today = today,
      startDate = today.minusMonths(2),
      endDate = today.plusMonths(2),
      content = persistentMapOf(),
      monthNames = localizedDateFormatter.getMonthNames(),
      daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
    )
  }

  fun createWithContent(
    startDate: LocalDate,
    endDate: LocalDate,
    content: ImmutableMap<LocalDate, AppCalendarDateInfo>,
  ): AppCalendarConfig {
    val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    return AppCalendarConfig(
      today = today,
      startDate = startDate,
      endDate = endDate,
      content = content,
      monthNames = localizedDateFormatter.getMonthNames(),
      daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
    )
  }

  private fun createWithInstances(
    instances: List<ProductInstance>,
  ): AppCalendarConfig {
    val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val startDate = instances
      .minBy { instance -> instance.expirationDate }
      .expirationDate
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    val endDate = instances
      .maxBy { instance -> instance.expirationDate }
      .expirationDate
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    val dateWithShapes = getDateWithShapes(instances, today)

    return AppCalendarConfig(
      today = today,
      startDate = startDate.minusMonths(1),
      endDate = endDate.plusMonths(1),
      content = dateWithShapes,
      monthNames = localizedDateFormatter.getMonthNames(),
      daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
    )
  }

  private fun getDateWithShapes(
    instances: List<ProductInstance>,
    today: LocalDate
  ): ImmutableMap<LocalDate, AppCalendarDateInfo> {
    val instancesStatusByDate = instancesStatusByDate(instances, today)
    return instancesStatusByDate
      .mapValues { (date, status) ->
        val hasPrevDay = instancesStatusByDate.containsKey(date.plus(-1, DateTimeUnit.DAY))
        val hasNextDay = instancesStatusByDate.containsKey(date.plus(1, DateTimeUnit.DAY))

        val shapePosition = when {
          date == today && !hasPrevDay && !hasNextDay -> ShapePosition.Single
          date == today && hasPrevDay && !hasNextDay -> ShapePosition.End
          !hasPrevDay && !hasNextDay -> ShapePosition.Single
          !hasPrevDay && hasNextDay -> ShapePosition.Start
          hasPrevDay && !hasNextDay -> ShapePosition.End
          else -> ShapePosition.Middle
        }

        AppCalendarDateInfo(status, shapePosition)
      }.toImmutableMap()
  }

  private fun instancesStatusByDate(
    instances: List<ProductInstance>,
    today: LocalDate
  ): Map<LocalDate, InstanceStatus?> {
    return buildMap {
      instances.forEach { instance ->
        val date = instance.expirationDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
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

      // Ensure today is always in the map, even if no products
      if (!containsKey(today)) {
        put(today, null)
      }
    }
  }
}
