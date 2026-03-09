package com.alorma.caducity.ui.screen.category.detail

import com.alorma.caducity.R
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.config.time.RelativeTimeFormatter
import com.alorma.caducity.domain.model.CategoryDetail
import com.alorma.caducity.domain.model.ProductItems
import com.alorma.caducity.ui.components.calendar.AppCalendarConfigMapper
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek

class CategoryDetailMapper(
  private val appClock: AppClock,
  private val relativeTimeFormatter: RelativeTimeFormatter,
  private val appCalendarConfigMapper: AppCalendarConfigMapper,
  private val stringProvider: StringProvider,
) {
  fun mapToCategoryDetail(
    categoryDetail: CategoryDetail,
    firstDayOfWeek: DayOfWeek,
    productItems: ProductItems,
  ): CategoryDetailState {
    val categoryUiModel =
      CategoryDetailUiModel(
        name = categoryDetail.category.name,
        description = categoryDetail.category.description,
      )

    val productTabs =
      categoryDetail.products
        .map { product ->
          CategoryProductTabUiModel(
            id = product.id,
            categoryId = categoryDetail.category.id,
            name = product.name,
          )
        }.toMutableList()

    // Add "Other" tab only if there are standalone items
    if (categoryDetail.hasStandaloneItems) {
      productTabs.add(
        CategoryProductTabUiModel(
          id = null,
          categoryId = categoryDetail.category.id,
          name = stringProvider.getString(R.string.category_detail_product_other),
        ),
      )
    }

    val today = appClock.nowDate()

    // Build calendar from product items instead of all category items
    val calendarDatedContent =
      productItems.datedItemsGroups
        .map { datedItems ->
          DateItemsUiModel(
            text = relativeTimeFormatter.format(today, datedItems.date),
            status = datedItems.status,
            date = datedItems.date,
            items = kotlinx.collections.immutable.persistentListOf(), // Empty - calendar doesn't need item details
          )
        }.toImmutableList()

    val startDate = productItems.datedItemsGroups.minOfOrNull { it.date } ?: today
    val endDate = productItems.datedItemsGroups.maxOfOrNull { it.date } ?: today

    val appCalendarConfig =
      appCalendarConfigMapper.createWithDatedContent(
        startDate = startDate,
        endDate = endDate,
        datedContent = calendarDatedContent,
        firstDayOfWeek = firstDayOfWeek,
      )

    // Return Empty state if there are no product tabs
    return if (productTabs.isEmpty()) {
      CategoryDetailState.Empty(
        today = today,
        category = categoryUiModel,
        appCalendarConfig = appCalendarConfig,
      )
    } else {
      CategoryDetailState.Success(
        today = today,
        category = categoryUiModel,
        appCalendarConfig = appCalendarConfig,
        productTabs =
          productTabs
            .sortedWith(
              compareBy({ it.id == null }, { it.name }),
            ).toImmutableList(),
      )
    }
  }
}
