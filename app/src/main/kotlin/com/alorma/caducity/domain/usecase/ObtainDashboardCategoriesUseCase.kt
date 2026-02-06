package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.CalendarData
import com.alorma.caducity.domain.model.DashboardCategory
import com.alorma.caducity.domain.model.DashboardData
import com.alorma.caducity.domain.model.DashboardSummary
import com.alorma.caducity.domain.model.DateStatus
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class ObtainDashboardCategoriesUseCase(
  private val appClock: AppClock,
  private val categoryDataSource: CategoryDataSource,
  private val expirationThresholds: ExpirationThresholds,
) {

  fun obtainCategories(): Flow<DashboardData> {
    return categoryDataSource.getCategories().map { categories ->
      // Collect all active items from all categories for summary
      val allActiveItems = categories.flatMap { categoryWithItems ->
        categoryWithItems.products.flatMap { product -> product.items } + categoryWithItems.standaloneItems
      }.filter { it.status != ItemStatus.Frozen && it.status != ItemStatus.Consumed }

      // Calculate summary counts by status
      val statusCounts = allActiveItems
        .groupBy { it.status }
        .mapValues { (_, items) -> items.size }

      val summary = DashboardSummary(statusCounts = statusCounts)

      // Build dashboard categories with calendar data (no item details)
      val dashboardCategories = categories.map { categoryWithItems ->
        // Get all active items for this category
        val categoryActiveItems = buildList {
          addAll(categoryWithItems.products.flatMap { it.items })
          addAll(categoryWithItems.standaloneItems)
        }.filter { it.status != ItemStatus.Frozen && it.status != ItemStatus.Consumed }

        // Group items by date for calendar - only dates, statuses, and counts
        val dateStatuses = categoryActiveItems
          .groupBy<_, LocalDate> { it.expirationDate.date() }
          .map { (date, items) ->
            DateStatus(
              date = date,
              status = calculateStatus(date),
              itemCount = items.size,
            )
          }
          .sortedBy { it.date }

        DashboardCategory(
          category = categoryWithItems.category,
          calendarData = CalendarData(dateStatuses = dateStatuses),
        )
      }

      DashboardData(
        summary = summary,
        categories = dashboardCategories,
      )
    }
  }

  private fun calculateStatus(expirationDate: LocalDate): ItemStatus {
    return ItemStatus.calculateStatus(
      expirationDate = expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold,
    )
  }
}
