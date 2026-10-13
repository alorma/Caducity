package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.AddedItems
import com.alorma.caducity.domain.model.PantryError
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Adds [quantity] independent items (same date) to the product named [name], creating it if needed. */
class AddItemUseCase(
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  suspend fun add(
    name: String,
    categoryId: String,
    date: LocalDate,
    quantity: Int,
    frozen: Boolean,
  ): Result<AddedItems> {
    if (name.isBlank()) return Result.failure(PantryError.BlankName)
    if (quantity !in 1..MAX_QUANTITY) return Result.failure(PantryError.InvalidQuantity(quantity))

    val remainingDays = date.let { appClock.nowDate().daysUntil(it) }
    // Frozen items need a date that is not in the past (P4); otherwise past dates are allowed (P15).
    if (frozen && remainingDays < 0) return Result.failure(PantryError.PastDateWhenFrozen)

    return try {
      Result.success(
        pantryDataSource.addItems(
          categoryId = categoryId,
          productName = name,
          expiration = date.toExpirationInstant(),
          quantity = quantity,
          frozenRemainingDays = remainingDays.takeIf { frozen },
        ),
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  companion object {
    /** Maximum quantity per add (P22). */
    const val MAX_QUANTITY = 20
  }
}
