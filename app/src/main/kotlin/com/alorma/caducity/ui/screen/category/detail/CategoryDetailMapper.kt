package com.alorma.caducity.ui.screen.category.detail

import com.alorma.caducity.R
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.DetailProduct
import com.alorma.caducity.domain.model.CategoryDetail
import com.alorma.caducity.domain.model.ProductItem
import com.alorma.caducity.ui.components.calendar.AppCalendarConfigMapper
import com.alorma.caducity.config.time.RelativeTimeFormatter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class CategoryDetailMapper(
  private val appClock: AppClock,
  private val relativeTimeFormatter: RelativeTimeFormatter,
  private val appCalendarConfigMapper: AppCalendarConfigMapper,
  private val stringProvider: StringProvider,
) {

  fun mapToCategoryDetail(
    categoryDetail: CategoryDetail,
    firstDayOfWeek: DayOfWeek,
  ): CategoryDetailState.Success {

    val categoryUiModel = CategoryDetailUiModel(
      id = categoryDetail.category.id,
      name = categoryDetail.category.name,
      description = categoryDetail.category.description,
    )

    // Map products to UI models (empty or with items)
    val productTabs = categoryDetail.products.map { product ->
      if (product.datedItemsGroups.isEmpty()) {
        CategoryDetailProductTabUiModel.Empty(
          id = product.id,
          name = product.name,
        )
      } else {
        CategoryDetailProductTabUiModel.WithItems(
          id = product.id,
          name = product.name,
          datedItemsGroups = product.datedItemsGroups.map { datedItems ->
            mapProductDatedContent(
              product = product,
              datedItems = datedItems
            )
          }.toImmutableList(),
        )
      }
    }.toMutableList()

    // Add "Other" tab for standalone items if they exist
    if (categoryDetail.standaloneItems.isNotEmpty()) {
      val otherTab = CategoryDetailProductTabUiModel.WithItems(
        id = "other",
        name = stringProvider.getString(R.string.category_detail_product_other),
        datedItemsGroups = listOf(
          DateItemsUiModel(
            text = "",
            status = ItemStatus.Fresh,
            date = appClock.nowDate(),
            items = categoryDetail.standaloneItems.map { item ->
              ItemDetailUiModel(
                id = item.id,
                expirationDate = appClock.nowDate(),
                status = ItemStatus.Fresh,
                text = item.name,
              )
            }.toImmutableList(),
          )
        ).toImmutableList(),
      )
      productTabs.add(otherTab)
    }

    val today = appClock.nowDate()

    // Collect all dates from products with items for calendar
    val productsWithItems = categoryDetail.products.filter { it.datedItemsGroups.isNotEmpty() }
    val allDates = productsWithItems.flatMap { product ->
      product.datedItemsGroups.map { it.date }
    }
    val startDate = allDates.minOrNull() ?: today
    val endDate = allDates.maxOrNull() ?: today

    // Create calendar config with all dated content from products with items
    val allDatedContents = productsWithItems.flatMap { product ->
      product.datedItemsGroups.map { datedItems ->
        mapProductDatedContent(product = product, datedItems = datedItems)
      }
    }.toImmutableList()

    val appCalendarConfig = appCalendarConfigMapper.createWithDatedContent(
      startDate = startDate,
      endDate = endDate,
      datedContent = allDatedContents,
      firstDayOfWeek = firstDayOfWeek,
    )

    return CategoryDetailState.Success(
      today = today,
      category = categoryUiModel,
      appCalendarConfig = appCalendarConfig,
      productTabs = productTabs.sortedBy { it.name }.toImmutableList(),
    )
  }

  private fun mapProductDatedContent(
    product: DetailProduct,
    datedItems: com.alorma.caducity.domain.model.ProductDatedItems,
  ): DateItemsUiModel {
    return DateItemsUiModel(
      text = relativeTimeFormatter.format(datedItems.date),
      status = datedItems.status,
      date = datedItems.date,
      items = datedItems.items.map { item ->
        mapItemToUi(
          item = item,
          status = datedItems.status,
          expirationDate = datedItems.date,
        )
      }.toImmutableList(),
    )
  }

  private fun mapItemToUi(
    item: ProductItem,
    expirationDate: LocalDate,
    status: ItemStatus,
  ): ItemDetailUiModel {
    return ItemDetailUiModel(
      id = item.id,
      expirationDate = expirationDate,
      status = status,
      text = item.name,
    )
  }
}
