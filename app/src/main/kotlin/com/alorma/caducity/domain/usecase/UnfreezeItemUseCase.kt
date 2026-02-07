package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.InstanceActionResult

class UnfreezeItemUseCase(
  private val itemDataSource: ItemDataSource,
) {

  suspend fun unfreezeItem(itemId: String): InstanceActionResult<Unit> {
    return try {
      itemDataSource.unfreezeItem(itemId)
      InstanceActionResult.Success(Unit)
    } catch (e: Exception) {
      InstanceActionResult.Failure(InstanceActionError.InstanceNotFound)
    }
  }
}
