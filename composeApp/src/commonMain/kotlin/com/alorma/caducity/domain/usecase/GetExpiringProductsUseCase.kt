package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant

/**
 * Use case for getting products that are expiring soon.
 * Filters products based on the expiration threshold from ExpirationThresholds.
 */
class GetExpiringProductsUseCase(
  private val productDataSource: ProductDataSource,
  private val expirationThresholds: ExpirationThresholds,
  private val clock: AppClock,
) {

  /**
   * Returns a list of products that are expiring within the configured threshold.
   * Only includes products with instances that have expiration dates within the threshold period.
   */
  suspend fun invoke(): List<ProductWithInstances> {
    val currentTime = clock.now()
    val thresholdTime = currentTime + expirationThresholds.soonExpiringThreshold

    // Get all products and filter by expiration date
    val allProducts = productDataSource.products.first()

    return allProducts
      .filter { productWithInstances ->
        // Check if any instance is expiring within threshold
        productWithInstances.instances.any { instance ->
          instance.expirationDate <= thresholdTime
        }
      }
      .map { productWithInstances ->
        // For each product, only include instances that are expiring
        productWithInstances.copy(
          instances = productWithInstances.instances.filter { instance ->
            instance.expirationDate <= thresholdTime
          }.toImmutableList()
        )
      }
      .sortedBy { productWithInstances ->
        // Sort by earliest expiration date
        productWithInstances.instances.minOfOrNull { it.expirationDate }
      }
  }
}
