package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
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
  suspend fun invoke(): List<ProductWithInstances> {
    // Get all products and filter by status
    val allProducts = productDataSource.products.first()

    return allProducts
      .filter { productWithInstances ->
        // Check if any instance is expiring or expired
        productWithInstances.instances.any { instance ->
          instance.status == InstanceStatus.ExpiringSoon || instance.status == InstanceStatus.Expired
        }
      }
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
