package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObtainDashboardProductsUseCase(
  private val productDataSource: ProductDataSource,
) {

  fun obtainProducts(): Flow<List<ProductWithInstances>> {
    return productDataSource
      .products
      .map { products ->
        products.sortedBy { product ->
          product.instances.minOfOrNull { instance -> instance.expirationDate }
        }
      }
  }
}
