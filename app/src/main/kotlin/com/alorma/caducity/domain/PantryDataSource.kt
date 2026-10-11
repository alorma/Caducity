package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.AddedBatch
import com.alorma.caducity.domain.model.BatchSnapshot
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ProductUsage
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

/** Transactional operations of the redesigned screens (add, edit, move, discard, categories). */
interface PantryDataSource {
  /**
   * Finds or creates the product named [productName] in [categoryId] and inserts [quantity] independent
   * items with the same expiration. When [frozenRemainingDays] is set the items are created frozen.
   */
  suspend fun addBatch(
    categoryId: String,
    productName: String,
    expiration: Instant,
    quantity: Int,
    frozenRemainingDays: Int?,
  ): AddedBatch

  /** Removes the items of [batch] and the product when the batch created it and it is left empty. */
  suspend fun undoAdd(batch: AddedBatch)

  /**
   * Deletes the units of [batch] (removing it from the batch in the UI) and the products left empty that the
   * batch had created. Returns what was deleted so [restoreBatch] can bring it back unchanged.
   */
  suspend fun removeBatch(batch: AddedBatch): BatchSnapshot

  /** Inserts again what [removeBatch] deleted: same ids, dates and creation times. */
  suspend fun restoreBatch(snapshot: BatchSnapshot)

  /** Edits a single unit; [frozenRemainingDays] is applied only when the unit is frozen. */
  suspend fun updateItem(
    itemId: String,
    categoryId: String,
    productName: String,
    expiration: Instant,
    frozenRemainingDays: Int?,
  )

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

  /** Items that are neither consumed nor discarded. Re-emits on any change. */
  fun observeActiveItems(): Flow<List<ActiveItem>>

  /** Categories ordered by `sortOrder`. Re-emits on any change. */
  fun observeCategories(): Flow<List<Category>>

  suspend fun getProductUsage(): List<ProductUsage>

  suspend fun getCategories(): List<Category>

  suspend fun getNextSortOrder(): Int

  suspend fun updateCategory(category: Category)

  suspend fun createCategories(categories: List<Category>)
}
