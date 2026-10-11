package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.InstanceActionError
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
}
