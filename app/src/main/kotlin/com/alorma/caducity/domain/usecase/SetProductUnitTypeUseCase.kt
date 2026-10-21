package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.UnitType
import kotlinx.coroutines.CancellationException

class SetProductUnitTypeUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun set(
    productId: String,
    unitType: UnitType,
  ): Result<Unit> =
    try {
      pantryDataSource.setProductUnitType(productId, unitType)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
