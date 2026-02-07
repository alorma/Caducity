package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.InstanceActionError

class UnfreezeItemUseCase(
  private val itemDataSource: ItemDataSource,
) {

  suspend fun unfreezeItem(itemId: String): Result<Unit> {
    return try {
      // Verify item exists before unfreezing
      val item = itemDataSource.getItem(itemId)
        ?: return Result.failure(InstanceActionError.InstanceNotFound)

      // If item is not frozen, it's already in the desired state - return success
      if (item.status != com.alorma.caducity.domain.model.ItemStatus.Frozen) {
        return Result.success(Unit)
      }

      itemDataSource.unfreezeItem(itemId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
