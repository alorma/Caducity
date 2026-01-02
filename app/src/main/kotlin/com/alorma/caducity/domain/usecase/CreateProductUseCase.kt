package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.base.main.clock.AppClock
import com.alorma.caducity.base.main.InstanceStatus
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateProductUseCase(
  private val productDataSource: ProductDataSource,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {

  @OptIn(ExperimentalUuidApi::class)
  suspend fun createProduct(
    name: String,
    description: String,
    instances: List<Pair<String, Instant>>,
  ): Result<String> {
    return try {
      val productId = Uuid.random().toString()
      val product = Product(
        id = productId,
        name = name,
        description = description,
      )

      val now = appClock.now()
      val productInstances = instances.map { (identifier, expirationDate) ->
        ProductInstance(
          id = Uuid.random().toString(),
          identifier = identifier,
          expirationDate = expirationDate,
          status = InstanceStatus.calculateStatus(
            expirationDate = expirationDate,
            now = now,
            soonExpiringThreshold = expirationThresholds.soonExpiringThreshold
          ),
        )
      }.toImmutableList()

      productDataSource.createProduct(product, productInstances)
      Result.success(productId)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
