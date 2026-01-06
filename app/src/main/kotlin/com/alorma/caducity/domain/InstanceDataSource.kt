package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.ProductInstance
import kotlinx.coroutines.flow.Flow

interface InstanceDataSource {
  fun getAllInstances(): Flow<List<ProductInstance>>
}