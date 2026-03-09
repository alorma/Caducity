package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.ProductDatedItems
import com.alorma.caducity.domain.model.ProductItem
import com.alorma.caducity.domain.model.ProductItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class GetProductItemsUseCase(
  private val itemDataSource: ItemDataSource,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {
  fun obtain(
    categoryId: String,
    productId: String?,
  ): Flow<ProductItems> =
    itemDataSource.getItemsByProduct(categoryId, productId).map { items ->
      // Separate items by status
      val activeItems =
        items.filter {
          it.status != ItemStatus.Frozen && it.status != ItemStatus.Consumed
        }
      val frozenItems = items.filter { it.status == ItemStatus.Frozen }
      val consumedItems = items.filter { it.status == ItemStatus.Consumed }

      // Group active items by date
      val dates: List<LocalDate> =
        activeItems
          .map { it.expirationDate.date() }
          .distinct()
          .sorted()

      val datedItemsGroups: List<ProductDatedItems> =
        dates.map { date ->
          val itemsForDate =
            activeItems
              .filter { it.expirationDate.date() == date }
              .map { item ->
                ProductItem(
                  id = item.id,
                  name = item.identifier.takeIf { it.isNotEmpty() } ?: "",
                  packSize = item.packSize,
                )
              }

          ProductDatedItems(
            date = date,
            status = calculateStatus(date),
            items = itemsForDate,
          )
        }

      val frozenProductItems =
        frozenItems.map { item ->
          ProductItem(
            id = item.id,
            name = item.identifier.takeIf { it.isNotEmpty() } ?: "",
            packSize = item.packSize,
          )
        }

      val consumedProductItems =
        consumedItems.map { item ->
          ProductItem(
            id = item.id,
            name = item.identifier.takeIf { it.isNotEmpty() } ?: "",
            packSize = item.packSize,
          )
        }

      ProductItems(
        datedItemsGroups = datedItemsGroups,
        frozenItems = frozenProductItems,
        consumedItems = consumedProductItems,
      )
    }

  private fun calculateStatus(expirationDate: LocalDate): ItemStatus =
    ItemStatus.calculateStatus(
      expirationDate = expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold,
    )
}
