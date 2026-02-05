package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.RoomEntityMapper
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.time.Duration.Companion.days

class RoomCategoryDataSource(
  database: AppDatabase,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) : CategoryDataSource {

  private val categoryDao = database.categoryDao()
  private val itemDao = database.itemDao()
  private val mapper = RoomEntityMapper(appClock, expirationThresholds)

  override fun getCategories(): Flow<ImmutableList<CategoryWithItems>> {
    val daoFlow = categoryDao.getAllCategoriesWithItems()

    return daoFlow.map { roomEntities ->
      roomEntities
        .map { mapper.mapCategoryWithItemsToModel(it) }
        .toImmutableList()
    }
  }

  override fun getCategory(categoryId: String): Flow<Result<CategoryWithItems>> {
    return categoryDao.getCategoryWithItems(categoryId)
      .map { roomEntity ->
        roomEntity?.let {
          Result.success(mapper.mapCategoryWithItemsToModel(it))
        } ?: Result.failure(NoSuchElementException("Category with id $categoryId not found"))
      }
  }

  override suspend fun createCategory(
    category: Category,
    items: ImmutableList<Item>,
  ) {
    categoryDao.insertCategory(mapper.mapCategoryToEntity(category))
    items.forEach { item ->
      itemDao.insertItem(mapper.mapItemToEntity(item, category.id))
    }
  }

  override suspend fun addItem(
    categoryId: String,
    item: NewItem
  ): String {
    val id = UUID.randomUUID().toString()

    itemDao.insertItem(
      mapper.mapNewItemToEntity(item, id = id, categoryId = categoryId),
    )
    return id
  }

  override suspend fun deleteItem(itemId: String) {
    itemDao.deleteItem(itemId)
  }

  override suspend fun deleteCategory(categoryId: String) {
    categoryDao.deleteCategory(categoryId)
  }

  override suspend fun getItem(itemId: String): Item? {
    return itemDao.getItem(itemId)?.let { mapper.mapItemToModel(it) }
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