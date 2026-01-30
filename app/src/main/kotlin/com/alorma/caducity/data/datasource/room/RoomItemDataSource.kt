package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.data.datasource.ItemRoomMapper
import com.alorma.caducity.domain.InstanceDataSource
import com.alorma.caducity.domain.model.ProductInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomItemDataSource(
  private val itemDao: ItemDao,
  private val mapper: ItemRoomMapper,
): InstanceDataSource {

  override fun getAllInstances(): Flow<List<ProductInstance>> {
    return itemDao.getAllItems().map { items ->
      items.map { item -> mapper.toModel(item) }
    }
  }
}