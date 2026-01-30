package com.alorma.caducity.ui.components.calendar

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.ui.components.shape.calculateShapeWithGaps
import com.alorma.caducity.ui.screen.category.detail.DateItemsUiModel
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class AppCalendarConfigMapper(
  private val appClock: AppClock,
  private val localizedDateFormatter: LocalizedDateFormatter,
) {

  fun createFromItems(
    items: List<Item>,
    firstDayOfWeek: DayOfWeek,
  ): AppCalendarConfig {
    val today = appClock.now().date()

    return if (items.isEmpty()) {
      createEmpty(today, firstDayOfWeek)
    } else {
      createWithItems(items, firstDayOfWeek)
    }
  }

  fun createEmpty(
    today: LocalDate = appClock.now().date(),
    firstDayOfWeek: DayOfWeek,
  ): AppCalendarConfig {
    return AppCalendarConfig(
      today = today,
      startDate = today.minusMonths(2),
      endDate = today.plusMonths(2),
      content = persistentMapOf(),
      monthNames = localizedDateFormatter.getMonthNames(),
      daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
      firstDayOfWeek = firstDayOfWeek,
    )
  }

  fun createWithDatedContent(
    startDate: LocalDate,
    endDate: LocalDate,
    datedContent: ImmutableList<DateItemsUiModel>,
    firstDayOfWeek: DayOfWeek,
  ): AppCalendarConfig {
    val today = appClock.now().date()

    val minDate = datedContent.minOfOrNull { it.date } ?: today
    val maxDate = datedContent.maxOfOrNull { it.date } ?: today

    // Create a map of dates that have content for quick lookup
    val datesWithContent = datedContent.associateBy { it.date }

    // Ensure today is included in the date range
    val actualMinDate = minOf(minDate, today)
    val actualMaxDate = maxOf(maxDate, today)

    // Convert date range to list for shape calculation
    val allDates = (actualMinDate..actualMaxDate).toList()

    val content = allDates.mapIndexed { index, date ->
      val dated = datesWithContent[date]

      val shape = allDates.calculateShapeWithGaps(
        index = index,
        hasContent = { date -> datesWithContent.any { it.key == date } },
      )

      date to AppCalendarDateInfo(
        status = dated?.status,
        shapePosition = shape,
      )
    }.toMap().toImmutableMap()

    return AppCalendarConfig(
      today = today,
      startDate = startDate,
      endDate = endDate,
      content = content,
      monthNames = localizedDateFormatter.getMonthNames(),
      daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
      firstDayOfWeek = firstDayOfWeek,
    )
  }

  private fun createWithItems(
    items: List<Item>,
    firstDayOfWeek: DayOfWeek,
  ): AppCalendarConfig {
    val today = appClock.now().date()

    val startDate = items
      .minBy { item -> item.expirationDate }
      .expirationDate
      .date()

    val endDate = items
      .maxBy { item -> item.expirationDate }
      .expirationDate
      .date()

    val dateWithShapes = getDateWithShapes(items, today)

    return AppCalendarConfig(
      today = today,
      startDate = startDate.minusMonths(1),
      endDate = endDate.plusMonths(1),
      content = dateWithShapes,
      monthNames = localizedDateFormatter.getMonthNames(),
      daysOfWeekNames = localizedDateFormatter.getDaysOfWeekNames(),
      firstDayOfWeek = firstDayOfWeek,
    )
  }

  private fun getDateWithShapes(
    items: List<Item>,
    today: LocalDate
  ): ImmutableMap<LocalDate, AppCalendarDateInfo> {
    val itemsStatusByDate = itemsStatusByDate(items, today)

    // Convert map to list of entries for shape calculation
    val dateEntries = itemsStatusByDate.entries.sortedBy { it.key }

    return dateEntries.mapIndexed { index, (date, status) ->
      val shape = dateEntries.calculateShapeWithGaps(
        index = index,
        hasContent = { date -> dateEntries.any { it.key == date } },
      )
      date to AppCalendarDateInfo(status, shape)
    }.toMap().toImmutableMap()
  }

  private fun itemsStatusByDate(
    items: List<Item>,
    today: LocalDate
  ): Map<LocalDate, InstanceStatus?> {
    return buildMap {
      items.forEach { item ->
        val date = item.expirationDate.date()
        val currentStatus = get(date)

        // Keep the most critical status (Expired > ExpiringSoon > Frozen > Fresh)
        val newStatus = when {
          currentStatus == InstanceStatus.Expired -> InstanceStatus.Expired
          item.status == InstanceStatus.Expired -> InstanceStatus.Expired
          currentStatus == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
          item.status == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
          currentStatus == InstanceStatus.Frozen -> InstanceStatus.Frozen
          item.status == InstanceStatus.Frozen -> InstanceStatus.Frozen
          else -> InstanceStatus.Fresh
        }

        put(date, newStatus)
      }

      // Ensure today is always in the map, even if no items
      if (!containsKey(today)) {
        put(today, null)
      }
    }
  }
}
