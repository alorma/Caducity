package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import kotlinx.coroutines.CancellationException

/** «Mou»: moves a unit to another category, finding or creating the homonymous product there (P12). */
class MoveItemToCategoryUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun move(
    itemId: String,
    categoryId: String,
  ): Result<Unit> =
    try {
      pantryDataSource.moveItem(itemId, categoryId)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
