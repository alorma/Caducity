package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.AddedBatch
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ProductUsage
import kotlin.time.Instant

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

  suspend fun getProductUsage(): List<ProductUsage>

  suspend fun getCategories(): List<Category>

  suspend fun getNextSortOrder(): Int

  suspend fun updateCategory(category: Category)

  suspend fun createCategories(categories: List<Category>)
}
