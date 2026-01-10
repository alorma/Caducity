package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.NewProductInstance
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

class AddInstanceToProductUseCase(
  private val productDataSource: ProductDataSource,
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
