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
        productWithInstances.copy(
          instances = productWithInstances.instances.filter { instance ->
            instance.status == InstanceStatus.ExpiringSoon || instance.status == InstanceStatus.Expired
          }.toImmutableList()
        )
      }
      .sortedBy { productWithInstances ->
        // Sort by earliest expiration date
        productWithInstances.instances.minOfOrNull { it.expirationDate }
      }
  }
}
