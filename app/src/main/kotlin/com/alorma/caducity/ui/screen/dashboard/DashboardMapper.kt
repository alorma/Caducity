package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.config.time.RelativeTimeFormatter
import com.alorma.caducity.domain.model.DashboardData
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.calendar.AppCalendarConfigMapper
import com.alorma.caducity.ui.screen.category.detail.DateItemsUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek

class DashboardMapper(
  private val appClock: AppClock,
  private val relativeTimeFormatter: RelativeTimeFormatter,
  private val appCalendarConfigMapper: AppCalendarConfigMapper,
) {

  fun mapToPerCategoryState(
    dashboardData: DashboardData,
    firstDayOfWeek: DayOfWeek,
  ): DashboardState.Success {
    val today = appClock.nowDate()

    // Map categories with calendar data
    val mapped = dashboardData.categories.map { dashboardCategory ->
      // Build calendar from calendar data (dates and statuses only, no items)
      val calendarDatedContent = dashboardCategory.calendarData.dateStatuses.map { dateStatus ->
        DateItemsUiModel(
          text = relativeTimeFormatter.format(today, dateStatus.date),
          status = dateStatus.status,
          date = dateStatus.date,
          items = persistentListOf(), // Empty - dashboard doesn't need item details
        )
      }.toImmutableList()

      val startDate = dashboardCategory.calendarData.dateStatuses.minOfOrNull { it.date } ?: today
      val endDate = dashboardCategory.calendarData.dateStatuses.maxOfOrNull { it.date } ?: today

      val appCalendarConfig = appCalendarConfigMapper.createWithDatedContent(
        startDate = startDate,
        endDate = endDate,
        datedContent = calendarDatedContent,
        firstDayOfWeek = firstDayOfWeek,
      )

      CategoryCalendarState(
        id = dashboardCategory.category.id,
        name = dashboardCategory.category.name,
        appCalendarConfig = appCalendarConfig,
      )
    }

    // Map summary from status counts
    val summary = DashboardSummary(
      expired = dashboardData.summary.statusCounts[ItemStatus.Expired] ?: 0,
      expiringSoon = dashboardData.summary.statusCounts[ItemStatus.ExpiringSoon] ?: 0,
      fresh = dashboardData.summary.statusCounts[ItemStatus.Fresh] ?: 0,
      frozen = dashboardData.summary.statusCounts[ItemStatus.Frozen] ?: 0,
    )

    return DashboardState.Success.PerCategory(
      summary = summary,
      categories = mapped,
    )
  }
}