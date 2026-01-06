package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.model.InstanceStatus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first

/**
 * Use case for getting products that are expiring soon or expired.
 * Filters products based on the instance status (ExpiringSoon or Expired).
 */
class GetExpiringProductsUseCase(
  private val productDataSource: ProductDataSource,
) {

  /**
   * Returns a list of products that are expiring soon or already expired.
   * Only includes products with instances that have ExpiringSoon or Expired status.
   */
  suspend fun load(): List<ProductWithInstances> {
    // Get all products filtered by expiring/expired status
    val statusFilter = ProductsListFilter.ByStatus(
      statuses = setOf(InstanceStatus.ExpiringSoon, InstanceStatus.Expired)
    )
    val filteredProducts = productDataSource.getProducts(statusFilter).first()

    return filteredProducts
      .map { productWithInstances ->
        // For each product, only include instances that are expiring or expired
        val filteredVariants = productWithInstances.variants.map { variantWithInstances ->
          variantWithInstances.copy(
            instances = variantWithInstances.instances.filter { instance ->
              instance.status == InstanceStatus.ExpiringSoon || instance.status == InstanceStatus.Expired
            }.toImmutableList()
          )
        }.filter { it.instances.isNotEmpty() }.toImmutableList()

        val filteredStandaloneInstances = productWithInstances.standaloneInstances.filter { instance ->
          instance.status == InstanceStatus.ExpiringSoon || instance.status == InstanceStatus.Expired
        }.toImmutableList()

        productWithInstances.copy(
          variants = filteredVariants,
          standaloneInstances = filteredStandaloneInstances
        )
      }
      .sortedBy { productWithInstances ->
        // Sort by earliest expiration date across all instances
        val allInstances = productWithInstances.variants.flatMap { it.instances } +
                         productWithInstances.standaloneInstances
        allInstances.minOfOrNull { it.expirationDate }
      }
  }
}
