package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.PantryError
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Edits a single unit (P20): if [name] matches no product of the category a new one is created, otherwise
 * the unit joins the existing one. The old product is kept even when it is left empty.
 */
class UpdateItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  suspend fun update(
    itemId: String,
    name: String,
    categoryId: String,
    date: LocalDate,
  ): Result<Unit> {
    if (name.isBlank()) return Result.failure(PantryError.BlankName)
    return try {
      val item = itemDataSource.getItem(itemId) ?: return Result.failure(PantryError.ItemNotFound)
      val remainingDays = appClock.nowDate().daysUntil(date)
      val frozen = item.pausedDate != null
      if (frozen && remainingDays < 0) return Result.failure(PantryError.PastDateWhenFrozen)

      pantryDataSource.updateItem(
        itemId = itemId,
        categoryId = categoryId,
        productName = name,
        expiration = date.toExpirationInstant(),
        frozenRemainingDays = remainingDays.takeIf { frozen },
      )
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
