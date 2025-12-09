package com.alorma.caducity.data.datasource

import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.coroutines.flow.Flow

interface ProductDataSource {
  fun getAllProductsWithInstances(): Flow<List<ProductWithInstances>>
}
