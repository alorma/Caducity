package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.domain.model.DashboardProducts
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ObtainDashboardProductsUseCase(
  private val productDataSource: ProductDataSource,
  private val expirationThresholds: ExpirationThresholds,
  private val appClock: AppClock,
) {

  fun obtainProducts(): Flow<DashboardProducts> {
    return productDataSource
      .products
      .onEach { products ->
        products.forEach { product ->
          println("Product ID: ${product.product.id}")
          product.instances.forEachIndexed { index, instance ->
            val date = instance
              .expirationDate
              .toLocalDateTime(TimeZone.currentSystemDefault())
              .date

            println("Instance: $index: [${instance.identifier}] $date")
          }
        }
      }
      .map { productsWithInstances ->
        val now = appClock.now()
        val soonThreshold = now + expirationThresholds.soonExpiringThreshold

        val expired = mutableListOf<ProductWithInstances>()
        val expiringSoon = mutableListOf<ProductWithInstances>()
        val fresh = mutableListOf<ProductWithInstances>()
        val empty = mutableListOf<ProductWithInstances>()

        productsWithInstances.forEach { productWithInstances ->
          if (productWithInstances.instances.isEmpty()) {
            empty.add(productWithInstances)
          } else {
            // Categorize product based on most restrictive instance status
            val hasExpiredInstance = productWithInstances.instances.any { it.expirationDate <= now }
            val hasExpiringSoonInstance = productWithInstances.instances.any {
              it.expirationDate > now && it.expirationDate <= soonThreshold
            }

            // Add product to only the most restrictive category
            when {
              hasExpiredInstance -> expired.add(productWithInstances)
              hasExpiringSoonInstance -> expiringSoon.add(productWithInstances)
              else -> fresh.add(productWithInstances)
            }
          }
        }

        DashboardProducts(
          expired = expired,
          expiringSoon = expiringSoon,
          fresh = fresh,
          empty = empty,
        )
      }
  }
}
