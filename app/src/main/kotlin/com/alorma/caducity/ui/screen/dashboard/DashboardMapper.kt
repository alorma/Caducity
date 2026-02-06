package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.ui.components.calendar.AppCalendarConfigMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.DayOfWeek

class DashboardMapper(
  private val appCalendarConfigMapper: AppCalendarConfigMapper,
) {

  fun mapToPerCategoryState(
    categories: ImmutableList<CategoryWithItems>,
    firstDayOfWeek: DayOfWeek,
  ): DashboardState.Success {
    val mapped = categories.map { category ->

      val items = buildList {
        addAll(category.products.flatMap { it.items })
        addAll(category.standaloneItems)
      }

      // Filter out consumed items for calendar display
      val activeItems = items.filter { it.status != ItemStatus.Consumed }

      CategoryCalendarState(
        id = category.category.id,
        name = category.category.name,
        appCalendarConfig = appCalendarConfigMapper.createFromItems(activeItems, firstDayOfWeek)
      )
    }

    // Filter out consumed items from all categories for summary calculation
    val allActiveItems = categories.flatMap { category ->
      category.products.flatMap { product -> product.items } + category.standaloneItems
    }.filter { it.status != ItemStatus.Consumed }

    val summary = calculateSummary(allActiveItems)

    return DashboardState.Success.PerCategory(
      summary = summary,
      categories = mapped,
    )
  }

  private fun calculateSummary(items: List<Item>): DashboardSummary {
    val expiredCount = getStatusCount(items, ItemStatus.Expired)
    val expiringSoonCount = getStatusCount(items, ItemStatus.ExpiringSoon)
    val freshCount = getStatusCount(items, ItemStatus.Fresh)
    val frozenCount = getStatusCount(items, ItemStatus.Frozen)

    return DashboardSummary(
      expired = expiredCount,
      expiringSoon = expiringSoonCount,
      fresh = freshCount,
      frozen = frozenCount,
    )
  }

  private fun getStatusCount(
    items: List<Item>,
    status: ItemStatus,
  ): Int {
    return items
      .filter { item -> item.status == status }
      .size
  }
}