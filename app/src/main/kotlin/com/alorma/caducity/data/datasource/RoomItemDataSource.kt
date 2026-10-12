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
    item: NewItem,
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

  override suspend fun getItem(itemId: String): Item? =
    itemDao.getItem(itemId)?.let { itemEntity ->
      itemMapper.toModel(itemEntity)
    }

  override suspend fun markItemAsConsumed(itemId: String) {
    itemDao.getItem(itemId)?.let { item ->
      val updatedItem =
        item.copy(
          consumedDate = appClock.now().toEpochMilliseconds(),
          pausedDate = null, // Clear frozen state if it was frozen
          remainingDays = null,
        )
      itemDao.updateItem(updatedItem)
    }
  }

  override suspend fun markItemAsTransformed(itemId: String) {
    itemDao.getItem(itemId)?.let { item ->
      itemDao.updateItem(
        item.copy(
          transformedDate = appClock.now().toEpochMilliseconds(),
          pausedDate = null,
          remainingDays = null,
        ),
      )
    }
  }

  override suspend fun freezeItem(
    itemId: String,
    remainingDays: Int,
  ) {
    itemDao.getItem(itemId)?.let { item ->
      val updatedItem =
        item.copy(
          pausedDate = appClock.now().toEpochMilliseconds(),
          remainingDays = remainingDays,
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

        val updatedItem =
          item.copy(
            expirationDate = newExpirationDate,
            pausedDate = null,
            remainingDays = null,
          )
        itemDao.updateItem(updatedItem)
      }
    }
  }

  override suspend fun rescheduleItem(
    itemId: String,
    newExpirationDateMillis: Long,
  ) {
    itemDao.getItem(itemId)?.let { item ->
      val updatedItem =
        item.copy(
          expirationDate = newExpirationDateMillis,
          // Clear frozen state when rescheduling
          pausedDate = null,
          remainingDays = null,
        )
      itemDao.updateItem(updatedItem)
    }
  }

  override suspend fun updatePackSize(
    itemId: String,
    newPackSize: Int,
  ) {
    itemDao.getItem(itemId)?.let { item ->
      val updatedItem = item.copy(packSize = newPackSize)
      itemDao.updateItem(updatedItem)
    }
  }

  override fun getAllItems(): Flow<List<Item>> =
    itemDao.getAllItems().map { entities ->
      entities.map { entity -> itemMapper.toModel(entity) }
    }
}
