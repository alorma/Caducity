package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.AddedItems
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.FrozenAmount
import com.alorma.caducity.domain.model.ItemSnapshot
import com.alorma.caducity.domain.model.ProductUsage
import com.alorma.caducity.domain.model.TransformIngredient
import com.alorma.caducity.domain.model.TransformResult
import com.alorma.caducity.domain.model.TransformedBatch
import com.alorma.caducity.domain.model.UnitType
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

/** Transactional operations of the redesigned screens (add, edit, move, discard, categories). */
interface PantryDataSource {
  /**
   * Finds or creates the product named [productName] in [categoryId] and inserts [quantity] independent
   * items with the same expiration. When [frozenRemainingDays] is set the items are created frozen.
   * [unitType] sets how the product is counted (changing the parent type resets its batches, see [setProductUnitType]);
   * with an [amount] (g or mL) a single measured batch is created and [quantity] is ignored.
   */
  suspend fun addItems(
    categoryId: String,
    productName: String,
    expiration: Instant,
    quantity: Int,
    frozenRemainingDays: Int?,
    unitType: UnitType? = null,
    amount: Int? = null,
  ): AddedItems

  /** Edits a single unit; [frozenRemainingDays] is applied only when the unit is frozen. Returns its product id. */
  suspend fun updateItem(
    itemId: String,
    categoryId: String,
    productName: String,
    expiration: Instant,
    frozenRemainingDays: Int?,
  ): String

  /** Moves a unit to [categoryId], finding or creating the homonymous product there. */
  suspend fun moveItem(
    itemId: String,
    categoryId: String,
  )

  suspend fun discard(
    itemIds: List<String>,
    at: Instant,
  )

  /** Undo of consume/discard. */
  suspend fun restore(itemIds: List<String>)

  /** Unfreezes the unit, giving it [newExpiration]. */
  suspend fun unfreeze(
    itemId: String,
    newExpiration: Instant,
  )

  /** Unfreezes the units in one transaction giving them [newExpiration]; units not frozen are skipped. Returns the ids unfrozen. */
  suspend fun unfreezeAll(
    itemIds: List<String>,
    newExpiration: Instant,
  ): List<String>

  /** Marks the units as consumed in one transaction; a frozen unit is unfrozen. */
  suspend fun consume(
    itemIds: List<String>,
    at: Instant,
  )

  /**
   * Freezes the units in one transaction; units already expired, or of a product that cannot be frozen, are skipped.
   * Returns the ids frozen.
   */
  suspend fun freeze(
    itemIds: List<String>,
    at: Instant,
  ): List<String>

  /**
   * Consumes [amount] (g or mL) of a measured batch; when it is the whole batch, or more, the batch is consumed. A frozen
   * batch stays frozen. Undo it with a snapshot taken before ([snapshots]).
   */
  suspend fun consumeAmount(
    itemId: String,
    amount: Int,
    at: Instant,
  )

  /**
   * Freezes [amount] (g or mL) of a measured batch: the batch keeps the rest and the frozen part is a new batch with the
   * same date. When it is the whole batch it is frozen as it is. Returns null when it cannot be frozen.
   */
  suspend fun freezeAmount(
    itemId: String,
    amount: Int,
    at: Instant,
  ): FrozenAmount?

  /**
   * Thaws [amount] (g or mL) of a frozen measured batch: the batch keeps the rest frozen and the thawed part is a new
   * batch with [newExpiration]. When it is the whole batch it is thawed as it is. Returns null when it is not frozen.
   */
  suspend fun unfreezeAmount(
    itemId: String,
    amount: Int,
    newExpiration: Instant,
  ): FrozenAmount?

  /** Deletes the items (undo of what created them). */
  suspend fun deleteItems(itemIds: List<String>)

  /** Gives all the units [newExpiration] in one transaction; frozen units are unfrozen. */
  suspend fun reschedule(
    itemIds: List<String>,
    newExpiration: Instant,
  )

  /**
   * In one transaction: creates a transformation, creates each of [results] (product found or created, one item per
   * serving, [TransformResult.frozenCount] of them frozen) and closes the [ingredients] as transformed at [at].
   * Returns what [undoTransform] needs.
   */
  suspend fun transform(
    ingredients: List<TransformIngredient>,
    results: List<TransformResult>,
    at: Instant,
  ): TransformedBatch

  /** Deletes the results and the products created, restores the ingredients as they were and drops the transformation. */
  suspend fun undoTransform(batch: TransformedBatch)

  /** Current state of the units, to hand to [restoreSnapshots] as the undo of a bulk action. */
  suspend fun snapshots(itemIds: List<String>): List<ItemSnapshot>

  suspend fun restoreSnapshots(snapshots: List<ItemSnapshot>)

  /** Items that are neither consumed nor discarded. Re-emits on any change. */
  fun observeActiveItems(): Flow<List<ActiveItem>>

  /** Categories ordered by `sortOrder`. Re-emits on any change. */
  fun observeCategories(): Flow<List<Category>>

  suspend fun getProductUsage(): List<ProductUsage>

  suspend fun getCategories(): List<Category>

  suspend fun getNextSortOrder(): Int

  suspend fun updateCategory(category: Category)

  suspend fun createCategories(categories: List<Category>)

  /**
   * Sets the product's «es pot congelar» override. `null`, or the same value as its category, stores no override so the
   * product keeps following the category.
   */
  suspend fun setProductCanFreeze(
    productId: String,
    canFreeze: Boolean?,
  )

  /**
   * Sets how the product is counted. Changing the parent type resets the quantity: to Mesurable the product keeps its
   * batch that expires first, with the default amount; from Mesurable every batch becomes a unit.
   */
  suspend fun setProductUnitType(
    productId: String,
    unitType: UnitType,
  )
}
