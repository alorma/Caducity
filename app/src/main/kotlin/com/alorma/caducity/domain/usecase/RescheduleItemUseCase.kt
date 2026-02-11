package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.InstanceActionError

class RescheduleItemUseCase(
  private val itemDataSource: ItemDataSource,
) {

  suspend fun rescheduleItem(itemId: String, newExpirationDateMillis: Long): Result<Unit> {
    return try {
      // Verify item exists before rescheduling
      val item = itemDataSource.getItem(itemId)
        ?: return Result.failure(InstanceActionError.InstanceNotFound)

      itemDataSource.rescheduleItem(itemId, newExpirationDateMillis)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
