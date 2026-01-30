package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.InstanceActionResult
import com.alorma.caducity.domain.model.ItemStatus

class ConsumeItemUseCase(
  private val categoryDataSource: CategoryDataSource,
) {

  suspend fun consumeItem(itemId: String): InstanceActionResult<Unit> {
    // Get the item to check its status
    val item = categoryDataSource.getItem(itemId)
      ?: return InstanceActionResult.Failure(InstanceActionError.InstanceNotFound)

    // Prevent consuming expired items
    if (item.status == ItemStatus.Expired) {
      return InstanceActionResult.Failure(InstanceActionError.CannotConsumeExpiredInstance(itemId))
    }

    categoryDataSource.markItemAsConsumed(itemId)
    return InstanceActionResult.Success(Unit)
  }

  suspend fun forceConsumeItem(itemId: String): InstanceActionResult<Unit> {
    // Get the item to verify it exists
    val item = categoryDataSource.getItem(itemId)
      ?: return InstanceActionResult.Failure(InstanceActionError.InstanceNotFound)

    // Force consume regardless of status
    categoryDataSource.markItemAsConsumed(itemId)
    return InstanceActionResult.Success(Unit)
  }
}
