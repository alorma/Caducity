package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.CategoryDetail
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.ProductDatedItems
import com.alorma.caducity.domain.model.ProductItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class ObtainCategoryDetailUseCase(
  private val appClock: AppClock,
  private val categoryDataSource: CategoryDataSource,
  private val expirationThresholds: ExpirationThresholds,
) {

  fun obtain(categoryId: String): Flow<Result<CategoryDetail>> {
    return categoryDataSource.getCategory(categoryId).map { result ->
      result.map { categoryWithItems ->
        // Build calendar data from all active items across all products
        val allActiveItems = categoryWithItems.products.flatMap { product ->
          product.items.filter { it.status != ItemStatus.Frozen && it.status != ItemStatus.Consumed }
        } + categoryWithItems.standaloneItems.filter {
          it.status != ItemStatus.Frozen && it.status != ItemStatus.Consumed
        }

        // Group items by date for calendar
        val calendarData: List<ProductDatedItems> = allActiveItems
          .groupBy { it.expirationDate.date() }
          .map { (date, items) ->
            ProductDatedItems(
              date = date,
              status = calculateStatus(date),
              items = items.map { item ->
                ProductItem(
                  id = item.id,
                  name = item.identifier.takeIf { it.isNotEmpty() } ?: "",
                )
              },
            )
          }
          .sortedBy { it.date }

        CategoryDetail(
          category = categoryWithItems.category,
          products = categoryWithItems.products.map { it.product },
          calendarData = calendarData,
        )
      }
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
