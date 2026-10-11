package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.PantryDataSource
import kotlinx.coroutines.CancellationException

/** Marks items as discarded («Llença»); a frozen item is unfrozen. */
class DiscardItemsUseCase(
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  suspend fun discard(itemIds: List<String>): Result<Unit> =
    try {
      pantryDataSource.discard(itemIds, appClock.now())
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
