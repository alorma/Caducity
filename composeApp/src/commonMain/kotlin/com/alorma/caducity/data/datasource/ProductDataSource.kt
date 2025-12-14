package com.alorma.caducity.data.datasource

import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ProductDataSource {
  val products: StateFlow<List<ProductWithInstances>>
  
  fun getProduct(productId: String): Flow<Result<ProductWithInstances>>
}
