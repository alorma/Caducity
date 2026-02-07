package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.InstanceActionResult

class UnfreezeItemUseCase(
  private val itemDataSource: ItemDataSource,
) {

  suspend fun unfreezeItem(itemId: String): InstanceActionResult<Unit> {
    return try {
      // Verify item exists before unfreezing
      val item = itemDataSource.getItem(itemId)
        ?: return InstanceActionResult.Failure(InstanceActionError.InstanceNotFound)

      // Only unfreeze if item is actually frozen
      if (item.status != com.alorma.caducity.domain.model.ItemStatus.Frozen) {
        return InstanceActionResult.Failure(InstanceActionError.InstanceNotFound)
      }

      itemDataSource.unfreezeItem(itemId)
      InstanceActionResult.Success(Unit)
    } catch (e: Exception) {
      InstanceActionResult.Failure(InstanceActionError.InstanceNotFound)
    }
  }
}
