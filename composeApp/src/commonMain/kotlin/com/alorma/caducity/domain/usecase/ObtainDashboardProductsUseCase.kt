package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.domain.model.DashboardProducts
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class ObtainDashboardProductsUseCase(
  private val productDataSource: ProductDataSource,
  private val appClock: AppClock,
) {
  private val soonExpiringThreshold: Duration = 7.days

  fun obtainProducts(): Flow<DashboardProducts> {
    println("Load products")
    return combine(
      productDataSource.getAllProducts(),
      productDataSource.getAllProductInstances()
    ) { products, instances ->
      println(instances)

      val now = appClock.now()
      val soonThreshold = now + soonExpiringThreshold

      // Group instances by expiration status
      val expired = instances.filter { it.expirationDate <= now }
      val expiringSoon =
        instances.filter { it.expirationDate > now && it.expirationDate <= soonThreshold }
      val fresh = instances.filter { it.expirationDate > soonThreshold }

      // Group instances by product
      fun groupByProduct(instances: List<ProductInstance>): List<ProductWithInstances> {
        return instances.groupBy { it.productId }
          .mapNotNull { (productId, productInstances) ->
            val product = products.find { it.id == productId }
            product?.let {
              ProductWithInstances(
                product = it,
                instances = productInstances
              )
            }
          }
      }

      // Find products with no instances
      val productsWithInstances = instances.map { it.productId }.toSet()
      val emptyProducts = products.filter { it.id !in productsWithInstances }
        .map { product ->
          ProductWithInstances(
            product = product,
            instances = emptyList()
          )
        }

      DashboardProducts(
        expired = groupByProduct(expired),
        expiringSoon = groupByProduct(expiringSoon),
        fresh = groupByProduct(fresh),
        empty = emptyProducts,
      )
    }
  }
}
