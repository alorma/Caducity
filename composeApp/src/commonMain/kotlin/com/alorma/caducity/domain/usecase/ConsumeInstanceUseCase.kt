package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource

class ConsumeInstanceUseCase(
  private val productDataSource: ProductDataSource,
) {

  suspend fun consumeInstance(instanceId: String): Result<Unit> {
    return try {
      productDataSource.markInstanceAsConsumed(instanceId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
