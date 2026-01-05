package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductInstanceGroup
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObtainDashboardProductsUseCase(
  private val productDataSource: ProductDataSource,
) {

  fun obtainProducts(
    filter: ProductsListFilter,
  ): Flow<ImmutableList<ProductWithInstances>> {
    return productDataSource
      .getProducts(filter)
      .map { products ->
        products
          .sortedBy { product ->
            // Sort by earliest expiration date across all instances
            val variantExpirations = product.variants.flatMap { it.instances }
            val allExpirations = variantExpirations + product.standaloneInstances
            allExpirations.minOfOrNull { it.expirationDate }
          }
          .toImmutableList()
      }
  }
}
