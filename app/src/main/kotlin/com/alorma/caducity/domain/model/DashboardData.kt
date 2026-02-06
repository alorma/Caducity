package com.alorma.caducity.domain.model

/**
 * Dashboard data for the main screen.
 * Contains summary counts and categories with calendar data only (no item details).
 */
data class DashboardData(
  val summary: DashboardSummary,
  val categories: List<DashboardCategory>,
)

data class DashboardSummary(
  val statusCounts: Map<ItemStatus, Int>,
)

data class DashboardCategory(
  val category: Category,
  val calendarData: CalendarData,
)
