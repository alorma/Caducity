package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.InstanceActionResult
import com.alorma.caducity.domain.model.InstanceStatus

class ConsumeInstanceUseCase(
  private val productDataSource: ProductDataSource,
) {

  suspend fun consumeInstance(instanceId: String): InstanceActionResult<Unit> {
    // Get the instance to check its status
    val instance = productDataSource.getInstance(instanceId)
      ?: return InstanceActionResult.Failure(InstanceActionError.InstanceNotFound)

    // Prevent consuming expired instances
    if (instance.status == InstanceStatus.Expired) {
      return InstanceActionResult.Failure(InstanceActionError.CannotConsumeExpiredInstance)
    }

    productDataSource.markInstanceAsConsumed(instanceId)
    return InstanceActionResult.Success(Unit)
  }
}
