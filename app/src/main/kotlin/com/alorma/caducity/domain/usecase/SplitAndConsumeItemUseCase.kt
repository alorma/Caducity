package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.NewItem

class SplitAndConsumeItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val consumeItemUseCase: ConsumeItemUseCase,
) {

  suspend fun splitAndConsume(
    categoryId: String,
    itemId: String,
    quantityToConsume: Int,
    forceConsume: Boolean = false,
  ): Result<Unit> {
    // Get the item to check its status and pack size
    val item = itemDataSource.getItem(itemId)
      ?: return Result.failure(InstanceActionError.InstanceNotFound)

    // Validate quantity
    if (quantityToConsume <= 0) {
      return Result.failure(IllegalArgumentException("Quantity must be positive"))
    }

    // Prevent consuming expired items (unless forced)
    if (!forceConsume && item.status == ItemStatus.Expired) {
      return Result.failure(InstanceActionError.CannotConsumeExpiredInstance(itemId))
    }

    // Single item or consume all from pack: use existing logic
    val packSize = item.packSize
    if (packSize == null || quantityToConsume >= packSize) {
      return if (forceConsume) {
        consumeItemUseCase.forceConsumeItem(itemId)
      } else {
        consumeItemUseCase.consumeItem(itemId)
      }
    }

    // Split pack: create new item with consumed quantity
    val consumedItem = NewItem(
      identifier = item.identifier,
      productId = item.productId,
      expirationDate = item.expirationDate,
      packSize = quantityToConsume,
    )

    val newItemId = itemDataSource.addItem(categoryId, consumedItem)

    // Mark the new split item as consumed
    if (forceConsume) {
      consumeItemUseCase.forceConsumeItem(newItemId)
    } else {
      consumeItemUseCase.consumeItem(newItemId)
    }

    // Update original pack size
    itemDataSource.updatePackSize(itemId, packSize - quantityToConsume)

    return Result.success(Unit)
  }
}
