package com.alorma.caducity.ui.screen.category.detail.product

import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.model.CategoryActiveCountRow
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Lightweight fake implementation of ItemDao for testing.
 * Uses in-memory MutableStateFlow for storage.
 */
class FakeItemDao(
  private val itemsInMemory: MutableStateFlow<List<ItemRoomEntity>>,
) : ItemDao {
  override fun getAllItems(): Flow<List<ItemRoomEntity>> = itemsInMemory

  override fun getCategoryItems(categoryId: String): Flow<List<ItemRoomEntity>> =
    itemsInMemory.map { items ->
      items.filter { it.categoryId == categoryId }
    }

  override fun getProductItems(
    categoryId: String,
    productId: String,
  ): Flow<List<ItemRoomEntity>> =
    itemsInMemory.map { items ->
      items.filter { it.categoryId == categoryId && it.productId == productId }
    }

  override fun getStandaloneItems(categoryId: String): Flow<List<ItemRoomEntity>> =
    itemsInMemory.map { items ->
      items.filter { it.categoryId == categoryId && it.productId == null }
    }

  override fun getActiveItems(): Flow<List<ItemRoomEntity>> =
    itemsInMemory.map { items -> items.filter { it.consumedDate == null && it.discardedDate == null } }

  override fun getActiveCountByCategory(): Flow<List<CategoryActiveCountRow>> =
    getActiveItems().map { items ->
      items.groupBy { it.categoryId }.map { (categoryId, group) -> CategoryActiveCountRow(categoryId, group.size) }
    }

  override suspend fun discardItem(
    itemId: String,
    discardedDate: Long,
  ) {
    itemsInMemory.value =
      itemsInMemory.value.map {
        if (it.id == itemId) it.copy(discardedDate = discardedDate, pausedDate = null, remainingDays = null) else it
      }
  }

  override suspend fun restoreItem(itemId: String) {
    itemsInMemory.value =
      itemsInMemory.value.map {
        if (it.id == itemId) it.copy(consumedDate = null, discardedDate = null) else it
      }
  }

  override suspend fun moveItem(
    itemId: String,
    categoryId: String,
    productId: String?,
  ) {
    itemsInMemory.value =
      itemsInMemory.value.map {
        if (it.id == itemId) it.copy(categoryId = categoryId, productId = productId) else it
      }
  }

  override suspend fun deleteItems(itemIds: List<String>) {
    itemsInMemory.value = itemsInMemory.value.filterNot { it.id in itemIds }
  }

  override suspend fun getItem(itemId: String): ItemRoomEntity? = itemsInMemory.value.firstOrNull { it.id == itemId }

  override suspend fun insertItem(item: ItemRoomEntity) {
    itemsInMemory.value += item
  }

  override suspend fun updateItem(item: ItemRoomEntity) {
    itemsInMemory.value =
      itemsInMemory.value.map {
        if (it.id == item.id) item else it
      }
  }

  override suspend fun deleteItem(itemId: String) {
    itemsInMemory.value = itemsInMemory.value.filterNot { it.id == itemId }
  }

  override suspend fun moveItemsToProduct(
    fromProductId: String,
    toProductId: String?,
  ) {
    itemsInMemory.value =
      itemsInMemory.value.map { item ->
        if (item.productId == fromProductId) item.copy(productId = toProductId) else item
      }
  }

  override suspend fun deleteActiveItemsByProduct(productId: String) {
    itemsInMemory.value =
      itemsInMemory.value.filterNot {
        it.productId == productId && it.consumedDate == null
      }
  }

  override suspend fun deleteConsumedItemsByProduct(
    categoryId: String,
    productId: String,
  ) {
    itemsInMemory.value =
      itemsInMemory.value.filterNot {
        it.categoryId == categoryId && it.productId == productId && it.consumedDate != null
      }
  }

  override suspend fun deleteAllItemsByProduct(
    categoryId: String,
    productId: String,
  ) {
    itemsInMemory.value =
      itemsInMemory.value.filterNot {
        it.categoryId == categoryId && it.productId == productId
      }
  }

  override suspend fun deleteConsumedStandaloneItems(categoryId: String) {
    itemsInMemory.value =
      itemsInMemory.value.filterNot {
        it.categoryId == categoryId && it.productId == null && it.consumedDate != null
      }
  }

  override suspend fun deleteAllStandaloneItems(categoryId: String) {
    itemsInMemory.value =
      itemsInMemory.value.filterNot {
        it.categoryId == categoryId && it.productId == null
      }
  }

  override suspend fun getAllItemsSync(): List<ItemRoomEntity> = itemsInMemory.value

  override suspend fun clearAllItems() {
    itemsInMemory.value = emptyList()
  }
}
