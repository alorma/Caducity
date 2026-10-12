package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.IngredientSpend
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.TransformIngredient
import com.alorma.caducity.domain.model.TransformResult
import com.alorma.caducity.domain.model.TransformedBatch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/** Turns ingredients into one or more new products in one step, and undoes it. */
class TransformItemsUseCase(
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  suspend fun transform(
    ingredients: List<IngredientSpend>,
    results: List<TransformResult>,
  ): Result<TransformedBatch> {
    if (ingredients.isEmpty() || results.isEmpty()) return Result.failure(PantryError.ItemNotFound)
    val today = appClock.nowDate()
    results.forEach { result ->
      if (result.name.isBlank()) return Result.failure(PantryError.BlankName)
      if (result.servings !in 1..MAX_SERVINGS) return Result.failure(PantryError.InvalidQuantity(result.servings))
      if (result.frozenCount !in 0..result.servings) {
        return Result.failure(PantryError.InvalidQuantity(result.frozenCount))
      }
      if (result.frozenCount > 0 && result.expiration < today) return Result.failure(PantryError.PastDateWhenFrozen)
    }

    return try {
      val active = pantryDataSource.observeActiveItems().first().associateBy { it.itemId }
      val spent = mutableSetOf<String>()
      val chosen =
        ingredients.map { ingredient ->
          val units =
            ingredient.candidateItemIds
              .mapNotNull { active[it] }
              .filter { it.itemId !in spent }
              .sortedBy { it.expiration }
          if (ingredient.count !in 1..units.size) return Result.failure(PantryError.InvalidQuantity(ingredient.count))
          val ids = units.take(ingredient.count).map { it.itemId }
          spent += ids
          TransformIngredient(ids)
        }
      Result.success(pantryDataSource.transform(chosen, results, appClock.now()))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun undo(batch: TransformedBatch): Result<Unit> =
    try {
      pantryDataSource.undoTransform(batch)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

  companion object {
    const val MAX_SERVINGS = 99
  }
}
