package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

class ObtainDashboardProductsUseCase(
  private val productDataSource: ProductDataSource,
) {

  fun obtainProducts(): Flow<ImmutableList<ProductWithInstances>> {
    return productDataSource.getProducts(
      filter = ProductsListFilter.All
    )
  }
}
