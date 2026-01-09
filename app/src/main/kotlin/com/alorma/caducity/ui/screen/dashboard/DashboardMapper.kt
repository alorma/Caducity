package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class DashboardMapper(
  private val appClock: AppClock,
  private val localizedDateFormatter: LocalizedDateFormatter,
) {

  fun mapToUnifiedState(
    instances: ImmutableList<ProductInstance>,
  ): DashboardState.Success {
    val summary = calculateSummary(instances)

    val calendarState = calculateCalendarState(
      instances = instances,
    )

    return DashboardState.Success.Unified(
      summary = summary,
      calendarState = calendarState,
    )
  }

  fun mapToPerProductState(
    products: ImmutableList<ProductWithInstances>,
  ): DashboardState.Success {
    val mapped = products.map { product ->

      val instances = buildList {
        addAll(product.variants.flatMap { it.instances })
        addAll(product.standaloneInstances)
      }

      ProductCalendarState(
        id = product.product.id,
        name = product.product.name,
        calendarState = calculateCalendarState(instances)
      )
    }

    val allInstances = products.flatMap { product ->
      product.variants.flatMap { variant -> variant.instances } + product.standaloneInstances
    }

    val summary = calculateSummary(allInstances)

    return DashboardState.Success.PerProduct(
      summary = summary,
      products = mapped,
    )
  }

  private fun calculateSummary(instances: List<ProductInstance>): DashboardSummary {
    val expiredCount = getStatusCount(instances, InstanceStatus.Expired)
    val expiringSoonCount = getStatusCount(instances, InstanceStatus.ExpiringSoon)
    val freshCount = getStatusCount(instances, InstanceStatus.Fresh)
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
    status: InstanceStatus,
  ): Int {
    return instances
      .filter { instance -> instance.status == status }
      .size
  }

  private fun calculateCalendarState(
    instances: List<ProductInstance>,
  ): CalendarState {
    val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    return if (instances.isEmpty()) {
      CalendarState(
        today = today,
        startDate = today.minusMonths(2),
        endDate = today.plusMonths(2),
        content = persistentMapOf(),
        monthNames = localizedDateFormatter.getMonthNames(),
        daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
      )
    } else {
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

      CalendarState(
        today = today,
        startDate = startDate.minusMonths(1),
        endDate = endDate.plusMonths(1),
        content = dateWithShapes,
        monthNames = localizedDateFormatter.getMonthNames(),
        daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
      )

    }
  }

  private fun getDateWithShapes(
    instances: List<ProductInstance>,
    today: LocalDate
  ): ImmutableMap<LocalDate, CalendarDateInfo> {
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

        CalendarDateInfo(status, shapePosition)
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