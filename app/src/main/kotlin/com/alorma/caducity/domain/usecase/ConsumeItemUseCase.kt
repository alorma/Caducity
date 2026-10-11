package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.coroutines.CancellationException

class ConsumeItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  /** Consumes all the units in one transaction (bulk actions of the status lists). */
  suspend fun consume(itemIds: List<String>): Result<Unit> =
    try {
      pantryDataSource.consume(itemIds, appClock.now())
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

  /** Permissive consume (P5): any existing item can be consumed, including expired ones. */
  suspend fun consume(itemId: String): Result<Unit> {
    itemDataSource.getItem(itemId) ?: return Result.failure(InstanceActionError.InstanceNotFound)
    itemDataSource.markItemAsConsumed(itemId)
    return Result.success(Unit)
  }

  // TEMP(fase 7): the old action sheet still uses the blocking variants.
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
