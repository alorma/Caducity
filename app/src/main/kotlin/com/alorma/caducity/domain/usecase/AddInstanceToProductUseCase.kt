package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.VariantDataSource
import com.alorma.caducity.domain.model.NewProductInstance
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddInstanceToProductUseCase(
  private val productDataSource: ProductDataSource,
  private val variantDataSource: VariantDataSource,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {

  @OptIn(ExperimentalUuidApi::class)
  suspend fun addInstance(
    productId: String,
    identifier: String,
    variantId: String? = null,
    expirationDate: Instant,
  ): Result<String> {
    return try {
      val instance = NewProductInstance(
        identifier = identifier,
        variantId = variantId,
        expirationDate = expirationDate,
      )

      val createdInstance = productDataSource.addInstance(productId, instance)
      Result.success(createdInstance)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
