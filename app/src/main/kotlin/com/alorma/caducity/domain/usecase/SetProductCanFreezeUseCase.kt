package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import kotlinx.coroutines.CancellationException

class SetProductCanFreezeUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  /** `null`, or the category's own value, makes the product follow its category again. */
  suspend fun set(
    productId: String,
    canFreeze: Boolean?,
  ): Result<Unit> =
    try {
      pantryDataSource.setProductCanFreeze(productId, canFreeze)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
