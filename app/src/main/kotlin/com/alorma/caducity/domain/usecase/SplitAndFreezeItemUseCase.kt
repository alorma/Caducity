package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.NewItem

class SplitAndFreezeItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val freezeItemUseCase: FreezeItemUseCase,
  private val appClock: AppClock,
) {
  suspend fun splitAndFreeze(
    categoryId: String,
    itemId: String,
    quantityToFreeze: Int,
  ): Result<Unit> {
    // Get the item to check its pack size
    val item =
      itemDataSource.getItem(itemId)
        ?: return Result.failure(InstanceActionError.InstanceNotFound)

    // Validate quantity
    if (quantityToFreeze <= 0) {
      return Result.failure(IllegalArgumentException("Quantity must be positive"))
    }

    // Check if item can be frozen (not already expired)
    val now = appClock.now()
    val nowDate = now.date()
    val expirationLocalDate = item.expirationDate.date()
    val remainingDays = (expirationLocalDate.toEpochDays() - nowDate.toEpochDays()).toInt()

    if (remainingDays < 0) {
      return Result.failure(InstanceActionError.CannotFreezeExpiredInstance)
    }

    // Single item or freeze all from pack: use existing logic
    val packSize = item.packSize
    if (packSize == null || quantityToFreeze >= packSize) {
      return freezeItemUseCase.freezeItem(itemId, item.expirationDate)
    }

    // Split pack: create new item with frozen quantity
    val frozenItem =
      NewItem(
        identifier = item.identifier,
        productId = item.productId,
        expirationDate = item.expirationDate,
        packSize = quantityToFreeze,
      )

    val newItemId = itemDataSource.addItem(categoryId, frozenItem)

    // Freeze the new split item
    freezeItemUseCase.freezeItem(newItemId, item.expirationDate)

    // Update original pack size
    itemDataSource.updatePackSize(itemId, packSize - quantityToFreeze)

    return Result.success(Unit)
  }

  suspend fun splitAndUnfreeze(
    categoryId: String,
    itemId: String,
    quantityToUnfreeze: Int,
  ): Result<Unit> {
    // Get the item to check its pack size
    val item =
      itemDataSource.getItem(itemId)
        ?: return Result.failure(InstanceActionError.InstanceNotFound)

    // Validate quantity
    if (quantityToUnfreeze <= 0) {
      return Result.failure(IllegalArgumentException("Quantity must be positive"))
    }

    // Single item or unfreeze all from pack: use existing logic
    val packSize = item.packSize
    if (packSize == null || quantityToUnfreeze >= packSize) {
      freezeItemUseCase.unfreezeItem(itemId)
      return Result.success(Unit)
    }

    // Split pack: create new item with unfrozen quantity
    val unfrozenItem =
      NewItem(
        identifier = item.identifier,
        productId = item.productId,
        expirationDate = item.expirationDate,
        packSize = quantityToUnfreeze,
      )

    val newItemId = itemDataSource.addItem(categoryId, unfrozenItem)

    // Unfreeze the new split item (calculates new expiration date)
    freezeItemUseCase.unfreezeItem(newItemId)

    // Update original pack size
    itemDataSource.updatePackSize(itemId, packSize - quantityToUnfreeze)

    return Result.success(Unit)
  }
}
