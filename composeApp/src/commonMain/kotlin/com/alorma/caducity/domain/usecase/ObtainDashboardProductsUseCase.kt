package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.domain.model.DashboardProducts
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
    return productDataSource.getAllProductInstances()
      .map { instances ->

        println(instances)

        val now = appClock.now()
        val soonThreshold = now + soonExpiringThreshold

        val expired = instances.filter {
          it.expirationDate <= now
        }
        val expiringSoon = instances.filter {
          it.expirationDate > now && it.expirationDate <= soonThreshold
        }
        val fresh = instances.filter {
          it.expirationDate > soonThreshold
        }

        DashboardProducts(
          expired = expired,
          expiringSoon = expiringSoon,
          fresh = fresh,
        )
      }
  }
}
