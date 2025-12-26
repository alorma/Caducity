package com.alorma.caducity.data.datasource

import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ProductDataSource {
  val products: StateFlow<ImmutableList<ProductWithInstances>>
  
  fun getProduct(productId: String): Flow<Result<ProductWithInstances>>
}
