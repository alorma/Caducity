package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomItemDataSource(
  private val itemDao: ItemDao,
  private val appClock: AppClock,
  private val itemMapper: ItemRoomMapper,
) : ItemDataSource {

  override suspend fun addItem(
    categoryId: String,
    item: NewItem
  ): String {
    val id = UUID.randomUUID().toString()

    itemDao.insertItem(
      itemMapper.toEntity(item, id = id, categoryId = categoryId),
    )
    return id
  }

  override suspend fun deleteItem(itemId: String) {
    itemDao.deleteItem(itemId)
  }

  override suspend fun getItem(itemId: String): Item? {
    return itemDao.getItem(itemId)?.let { itemEntity ->
      itemMapper.toModel(itemEntity)
    }
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

  override fun getItemsByProduct(categoryId: String, productId: String?): Flow<List<Item>> {
    return if (productId != null) {
      itemDao.getProductItems(categoryId, productId)
    } else {
      itemDao.getStandaloneItems(categoryId)
    }.map { entities ->
      entities.map { entity -> itemMapper.toModel(entity) }
    }
  }

  override fun getAllItems(): Flow<List<Item>> {
    return itemDao.getAllItems().map { entities ->
      entities.map { entity -> itemMapper.toModel(entity) }
    }
  }

  override suspend fun clearConsumedItems(categoryId: String, productId: String?) {
    if (productId != null) {
      itemDao.deleteConsumedItemsByProduct(categoryId, productId)
    } else {
      itemDao.deleteConsumedStandaloneItems(categoryId)
    }
  }

  override suspend fun clearAllItems(categoryId: String, productId: String?) {
    if (productId != null) {
      itemDao.deleteAllItemsByProduct(categoryId, productId)
    } else {
      itemDao.deleteAllStandaloneItems(categoryId)
    }
  }
}
