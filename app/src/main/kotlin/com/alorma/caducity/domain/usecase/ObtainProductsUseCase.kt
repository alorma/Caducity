package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

class ObtainProductsUseCase(
  private val productDataSource: ProductDataSource,
) {

  fun obtain(filter: ProductsListFilter): Flow<ImmutableList<ProductWithInstances>> {
    return productDataSource.getProducts(filter)
  }

}