package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.toModel
import com.alorma.caducity.data.datasource.room.toRoomEntity
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.ProductsListFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import java.util.UUID
import kotlin.time.Duration.Companion.days

class RoomCategoryDataSource(
  database: AppDatabase,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) : CategoryDataSource {

  private val categoryDao = database.categoryDao()
  private val itemDao = database.itemDao()

  override fun getCategories(filter: ProductsListFilter): Flow<ImmutableList<CategoryWithItems>> {
    val daoFlow = when (filter) {
      is ProductsListFilter.All -> {
        categoryDao.getAllCategoriesWithItems()
      }

      is ProductsListFilter.ByDate -> {
        val startOfDayMillis =
          filter.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val nextDayMillis = startOfDayMillis + 1.days.inWholeMilliseconds
        categoryDao.getCategoriesWithItemsByDate(startOfDayMillis, nextDayMillis)
      }

      is ProductsListFilter.ByDateRange -> {
        val startMillis =
          filter.startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = filter.endDate.atStartOfDayIn(TimeZone.currentSystemDefault())
          .toEpochMilliseconds() + 1.days.inWholeMilliseconds
        categoryDao.getCategoriesWithItemsByDateRange(startMillis, endMillis)
      }

      is ProductsListFilter.ByStatus -> {
        // Convert status filters to date range queries for SQL optimization
        when {
          filter.statuses.isEmpty() -> {
            categoryDao.getAllCategoriesWithItems()
          }

          filter.statuses.size == 1 -> {
            // Single status: fully optimized SQL query
            val status = filter.statuses.first()
            val (minDate, maxDate) = statusToDateRange(status)
            categoryDao.getCategoriesWithItemsByDateRange(minDate, maxDate)
          }

          else -> {
            // Multiple statuses: hybrid approach - SQL narrows range, then in-memory filter
            val dateRanges = filter.statuses.map { statusToDateRange(it) }
            val minDate = dateRanges.minOf { it.first }
            val maxDate = dateRanges.maxOf { it.second }
            categoryDao.getCategoriesWithItemsByDateRange(minDate, maxDate)
          }
        }
      }
    }

    return daoFlow.map { roomEntities ->
      // Filter consumed instances at the source
      val filteredEntities = roomEntities.map { it.filterConsumed() }
      val products = filteredEntities.map { it.toModel(appClock, expirationThresholds) }

      // Apply in-memory status filter only for multiple statuses
      // (to handle non-contiguous date ranges like Expired + Fresh)
      val filtered = if (filter is ProductsListFilter.ByStatus && filter.statuses.size > 1) {
        products.filter { categoryWithItems ->
          // Keep category if it has at least one item with the requested status
          val allItems = categoryWithItems.products.flatMap { it.items } +
              categoryWithItems.standaloneItems
          allItems.any { item ->
            item.status in filter.statuses
          }
        }
      } else {
        products
      }

      filtered.toImmutableList()
    }
  }

  private fun statusToDateRange(status: ItemStatus): Pair<Long, Long> {
    val now = appClock.now()
    val expiringSoonMillis =
      now.plus(expirationThresholds.soonExpiringThreshold).toEpochMilliseconds()

    // Calculate start of today for consistent date boundary handling
    val todayStartMillis = now
      .date()
      .atStartOfDayIn(TimeZone.currentSystemDefault())
      .toEpochMilliseconds()

    return when (status) {
      ItemStatus.Expired -> {
        // From epoch to yesterday (before today starts) - excludes items expiring today
        Pair(0L, todayStartMillis)
      }

      ItemStatus.ExpiringSoon -> {
        // From today (start of day) to (now + threshold) - includes items expiring today
        Pair(todayStartMillis, expiringSoonMillis)
      }

      ItemStatus.Fresh -> {
        Pair(expiringSoonMillis, Long.MAX_VALUE) // From (now + threshold) to infinity
      }

      ItemStatus.Frozen -> {
        // Frozen items don't have a date range filter - return all dates
        Pair(0L, Long.MAX_VALUE)
      }
    }
  }

  override fun getCategory(categoryId: String): Flow<Result<CategoryWithItems>> {
    return categoryDao.getCategoryWithItems(categoryId)
      .map { roomEntity ->
        roomEntity?.let {
          // Filter consumed items before converting
          Result.success(it.filterConsumed().toModel(appClock, expirationThresholds))
        } ?: Result.failure(NoSuchElementException("Category with id $categoryId not found"))
      }
  }

  override suspend fun createCategory(
    category: Category,
    items: ImmutableList<Item>,
  ) {
    categoryDao.insertCategory(category.toRoomEntity())
    items.forEach { item ->
      itemDao.insertItem(item.toRoomEntity(category.id))
    }
  }

  override suspend fun addItem(
    categoryId: String,
    item: NewItem
  ): String {
    val id = UUID.randomUUID().toString()

    itemDao.insertItem(
      item.toRoomEntity(id = id, categoryId = categoryId),
    )
    return id
  }

  override suspend fun deleteItem(itemId: String) {
    itemDao.deleteItem(itemId)
  }

  override suspend fun getItem(itemId: String): Item? {
    return itemDao.getItem(itemId)?.toModel(appClock, expirationThresholds)
  }

  override suspend fun markItemAsConsumed(itemId: String) {
    itemDao.getItem(itemId)?.let { item ->
      val updatedItem = item.copy(
        consumedDate = appClock.now().toEpochMilliseconds(),
        pausedDate = null, // Clear frozen state if it was frozen
        remainingDays = null
      )
      itemDao.updateItem(updatedItem)
    }
  }

  override suspend fun freezeItem(itemId: String, remainingDays: Int) {
    itemDao.getItem(itemId)?.let { item ->
      val updatedItem = item.copy(
        pausedDate = appClock.now().toEpochMilliseconds(),
        remainingDays = remainingDays
      )
      itemDao.updateItem(updatedItem)
    }
  }

  override suspend fun unfreezeItem(itemId: String) {
    itemDao.getItem(itemId)?.let { item ->
      val pausedDate = item.pausedDate
      val remainingDays = item.remainingDays

      if (pausedDate != null && remainingDays != null) {
        // Calculate new expiration date: now + remaining days
        val now = appClock.now()
        val newExpirationDate = now.toEpochMilliseconds() + (remainingDays.days.inWholeMilliseconds)

        val updatedItem = item.copy(
          expirationDate = newExpirationDate,
          pausedDate = null,
          remainingDays = null
        )
        itemDao.updateItem(updatedItem)
      }
    }
  }

  override suspend fun clearAllCategories() {
    categoryDao.clearAllCategories()
  }

  override suspend fun clearAllItems() {
    itemDao.clearAllItems()
  }
}