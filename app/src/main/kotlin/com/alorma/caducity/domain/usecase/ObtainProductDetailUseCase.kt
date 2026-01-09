package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.ProductInstanceComparator
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObtainProductDetailUseCase(
  private val productDataSource: ProductDataSource,
  private val instanceComparator: ProductInstanceComparator,
) {

  fun obtainProductDetail(productId: String): Flow<Result<ProductWithInstances>> {
    return productDataSource.getProduct(productId).map { result ->
      result.map { productWithInstances ->
        productWithInstances.copy(
          variants = productWithInstances.variants.map { variant ->
            variant.copy(
              instances = variant.instances.sortedWith(instanceComparator).toImmutableList()
            )
          }.toImmutableList(),
          standaloneInstances = productWithInstances.standaloneInstances
            .sortedWith(instanceComparator)
            .toImmutableList()
        )
      }
    }
  }
}
