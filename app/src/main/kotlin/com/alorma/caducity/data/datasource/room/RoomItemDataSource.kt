package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.data.datasource.ItemRoomMapper
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomItemDataSource(
  private val itemDao: ItemDao,
  private val mapper: ItemRoomMapper,
): ItemDataSource {

  override fun getAllItems(): Flow<List<Item>> {
    return itemDao.getAllItems().map { items ->
      items.map { item -> mapper.toModel(item) }
    }
  }
}