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

      CategoryCalendarState(
        id = category.category.id,
        name = category.category.name,
        appCalendarConfig = appCalendarConfigMapper.createFromItems(items, firstDayOfWeek)
      )
    }

    val allItems = categories.flatMap { category ->
      category.products.flatMap { product -> product.items } + category.standaloneItems
    }

    val summary = calculateSummary(allItems)

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