package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.data.datasource.InstanceRoomMapper
import com.alorma.caducity.domain.InstanceDataSource
import com.alorma.caducity.domain.model.ProductInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomInstanceDataSource(
  private val instanceDao: InstanceDao,
  private val mapper: InstanceRoomMapper,
): InstanceDataSource {

  override fun getAllInstances(): Flow<List<ProductInstance>> {
    return instanceDao.getAllInstances().map { instances ->
      instances.map { instance -> mapper.toModel(instance) }
    }
  }
}