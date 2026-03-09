package com.alorma.caducity.domain.model

/**
 * Simplified category detail for the category overview screen.
 * Product tabs load their own item data via ProductPageViewModel.
 * Calendar data contains only dates and statuses, no item details.
 */
data class CategoryDetail(
  val category: Category,
  val products: List<Product>,
  val calendarData: CalendarData,
  val hasStandaloneItems: Boolean,
)
