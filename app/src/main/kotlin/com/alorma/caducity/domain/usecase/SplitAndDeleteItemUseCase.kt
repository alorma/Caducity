package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.InstanceActionError

class SplitAndDeleteItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val deleteItemUseCase: DeleteItemUseCase,
) {
  suspend fun splitAndDelete(
    itemId: String,
    quantityToDelete: Int,
  ): Result<Unit> {
    // Get the item to check its pack size
    val item =
      itemDataSource.getItem(itemId)
        ?: return Result.failure(InstanceActionError.InstanceNotFound)

    // Validate quantity
    if (quantityToDelete <= 0) {
      return Result.failure(IllegalArgumentException("Quantity must be positive"))
    }

    // Single item or delete all from pack: use existing logic
    val packSize = item.packSize
    if (packSize == null || quantityToDelete >= packSize) {
      return deleteItemUseCase.deleteItem(itemId)
    }

    // Partial delete: just reduce pack size (no new item needed)
    itemDataSource.updatePackSize(itemId, packSize - quantityToDelete)

    return Result.success(Unit)
  }
}
