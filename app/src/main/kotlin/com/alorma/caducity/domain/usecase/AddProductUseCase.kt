package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.AddedItems
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.UnitType
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Adds a product named [name] to [categoryId] (creating it if needed) with [quantity] independent items (units) expiring on [date]; frozen items start paused. */
class AddProductUseCase(
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  suspend fun add(
    name: String,
    categoryId: String,
    date: LocalDate,
    quantity: Int,
    frozen: Boolean,
    unitType: UnitType? = null,
    /** g or mL of a measured product: a single batch is created and [quantity] is ignored. */
    amount: Int? = null,
  ): Result<AddedItems> {
    if (name.isBlank()) return Result.failure(PantryError.BlankName)
    if (quantity !in 1..MAX_QUANTITY) return Result.failure(PantryError.InvalidQuantity(quantity))
    if (amount != null && amount !in 1..UnitType.MAX_AMOUNT) return Result.failure(PantryError.InvalidAmount(amount))

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
          unitType = unitType,
          amount = amount,
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
