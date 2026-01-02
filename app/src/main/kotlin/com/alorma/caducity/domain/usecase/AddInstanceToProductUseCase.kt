package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.clock.AppClock
import com.alorma.caducity.base.main.InstanceStatus
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddInstanceToProductUseCase(
  private val productDataSource: ProductDataSource,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {

  @OptIn(ExperimentalUuidApi::class)
  suspend fun addInstance(
    productId: String,
    identifier: String,
    expirationDate: Instant,
  ): Result<String> {
    return try {
      val instanceId = Uuid.random().toString()
      val now = appClock.now()

      val instance = ProductInstance(
        id = instanceId,
        identifier = identifier,
        expirationDate = expirationDate,
        status = InstanceStatus.calculateStatus(
          expirationDate = expirationDate,
          now = now,
          soonExpiringThreshold = expirationThresholds.soonExpiringThreshold
        ),
      )

      productDataSource.addInstance(productId, instance)
      Result.success(instanceId)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
