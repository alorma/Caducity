package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.ItemStatus

class ConsumeItemUseCase(
  private val itemDataSource: ItemDataSource,
) {
  suspend fun consumeItem(itemId: String): Result<Unit> {
    // Get the item to check its status
    val item =
      itemDataSource.getItem(itemId)
        ?: return Result.failure(InstanceActionError.InstanceNotFound)

    // Prevent consuming expired items
    if (item.status == ItemStatus.Expired) {
      return Result.failure(InstanceActionError.CannotConsumeExpiredInstance(itemId))
    }

    itemDataSource.markItemAsConsumed(itemId)
    return Result.success(Unit)
  }

  suspend fun forceConsumeItem(itemId: String): Result<Unit> {
    // Get the item to verify it exists
    val item =
      itemDataSource.getItem(itemId)
        ?: return Result.failure(InstanceActionError.InstanceNotFound)

    // Force consume regardless of status
    itemDataSource.markItemAsConsumed(itemId)
    return Result.success(Unit)
  }
}
