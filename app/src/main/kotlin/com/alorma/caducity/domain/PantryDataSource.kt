package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.AddedItems
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ItemSnapshot
import com.alorma.caducity.domain.model.ProductUsage
import com.alorma.caducity.domain.model.TransformIngredient
import com.alorma.caducity.domain.model.TransformResult
import com.alorma.caducity.domain.model.TransformedBatch
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

/** Transactional operations of the redesigned screens (add, edit, move, discard, categories). */
interface PantryDataSource {
  /**
   * Finds or creates the product named [productName] in [categoryId] and inserts [quantity] independent
   * items with the same expiration. When [frozenRemainingDays] is set the items are created frozen.
   */
  suspend fun addItems(
    categoryId: String,
    productName: String,
    expiration: Instant,
    quantity: Int,
    frozenRemainingDays: Int?,
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
}
