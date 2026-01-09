package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductInstanceGroup
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObtainProductDetailUseCase(
  private val productDataSource: ProductDataSource,
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

  private val instanceComparator = compareBy<ProductInstance> {
    when (it.status) {
      InstanceStatus.Expired -> 0
      InstanceStatus.ExpiringSoon -> 1
      InstanceStatus.Fresh -> 2
      InstanceStatus.Frozen -> 3
      InstanceStatus.Consumed -> 4
    }
  }.thenBy { it.expirationDate }
}
