package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.domain.model.DashboardProducts
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class ObtainDashboardProductsUseCase(
  private val productDataSource: ProductDataSource,
  private val appClock: AppClock,
) {
  private val soonExpiringThreshold: Duration = 7.days

  fun obtainProducts(): Flow<DashboardProducts> {
    println("Load products")
    return productDataSource.getAllProductsWithInstances()
      .map { productsWithInstances ->
        val now = appClock.now()
        val soonThreshold = now + soonExpiringThreshold

        val expired = mutableListOf<ProductWithInstances>()
        val expiringSoon = mutableListOf<ProductWithInstances>()
        val fresh = mutableListOf<ProductWithInstances>()
        val empty = mutableListOf<ProductWithInstances>()

        productsWithInstances.forEach { productWithInstances ->
          if (productWithInstances.instances.isEmpty()) {
            empty.add(productWithInstances)
          } else {
            // Categorize instances by expiration status
            val expiredInstances =
              productWithInstances.instances.filter { it.expirationDate <= now }
            val expiringSoonInstances = productWithInstances.instances.filter {
              it.expirationDate > now && it.expirationDate <= soonThreshold
            }
            val freshInstances = productWithInstances.instances.filter { it.expirationDate > soonThreshold }

            // Add product to each category that has instances
            if (expiredInstances.isNotEmpty()) {
              expired.add(productWithInstances.copy(instances = productWithInstances.instances))
            }
            if (expiringSoonInstances.isNotEmpty()) {
              expiringSoon.add(productWithInstances.copy(instances = productWithInstances.instances))
            }
            if (freshInstances.isNotEmpty()) {
              fresh.add(productWithInstances.copy(instances = productWithInstances.instances))
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
