package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource

class DeleteInstanceUseCase(
  private val productDataSource: ProductDataSource,
) {

  suspend fun deleteInstance(instanceId: String): Result<Unit> {
    return try {
      productDataSource.deleteInstance(instanceId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
