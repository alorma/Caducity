package com.alorma.caducity.ui.screen.category.detail.product

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.ProductItems
import com.alorma.caducity.ui.screen.category.detail.DateItemsUiModel
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class ProductPageMapper(
  private val appClock: AppClock,
) {
  fun mapToUiModel(productItems: ProductItems): ProductPageState.Success {
    val datedItemsGroups = productItems.datedItemsGroups.map { datedItems ->
      val dateText = formatDate(datedItems.date)

      DateItemsUiModel(
        text = dateText,
        status = datedItems.status,
        date = datedItems.date,
        items = datedItems.items.map { item ->
          ItemDetailUiModel(
            id = item.id,
            expirationDate = datedItems.date,
            status = datedItems.status,
            text = item.name.ifEmpty { "Item" },
            packSize = item.packSize,
          )
        }.toImmutableList(),
      )
    }.toImmutableList()

    val frozenItems = productItems.frozenItems.map { item ->
      ItemDetailUiModel(
        id = item.id,
        expirationDate = appClock.now().date(),
        status = ItemStatus.Frozen,
        text = item.name.ifEmpty { "Item" },
        packSize = item.packSize,
      )
    }.toImmutableList()

    val consumedItems = productItems.consumedItems.map { item ->
      ItemDetailUiModel(
        id = item.id,
        expirationDate = appClock.now().date(),
        status = ItemStatus.Consumed,
        text = item.name.ifEmpty { "Item" },
        packSize = item.packSize,
      )
    }.toImmutableList()

    return ProductPageState.Success(
      datedItemsGroups = datedItemsGroups,
      frozenItems = frozenItems,
      consumedItems = consumedItems,
    )
  }

  private fun formatDate(date: LocalDate): String {
    val today = appClock.now().date()
    return when {
      date == today -> "Today"
      date == today.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
      date == today.plus(-1, DateTimeUnit.DAY) -> "Yesterday"
      else -> date.toString()
    }
  }
}
