package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.CalendarData
import com.alorma.caducity.domain.model.CategoryDetail
import com.alorma.caducity.domain.model.DateStatus
import com.alorma.caducity.domain.model.ItemStatus
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

        // Group items by date for calendar - only dates, statuses, and counts
        val dateStatuses = allActiveItems
          .groupBy { it.expirationDate.date() }
          .map { (date, items) ->
            DateStatus(
              date = date,
              status = calculateStatus(date),
              itemCount = items.size,
            )
          }
          .sortedBy { it.date }

        CategoryDetail(
          category = categoryWithItems.category,
          products = categoryWithItems.products.map { it.product },
          calendarData = CalendarData(dateStatuses = dateStatuses),
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
