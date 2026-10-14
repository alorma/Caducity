package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

interface ItemDataSource {
  suspend fun addItem(
    categoryId: String,
    item: NewItem,
  ): String

  suspend fun deleteItem(itemId: String)

  suspend fun getItem(itemId: String): Item?

  suspend fun markItemAsConsumed(itemId: String)

  /** [frozenAt] is when it went into the freezer; null means now. */
  suspend fun freezeItem(
    itemId: String,
    remainingDays: Int,
    frozenAt: Instant? = null,
  )

  suspend fun unfreezeItem(itemId: String)

  suspend fun rescheduleItem(
    itemId: String,
    newExpirationDateMillis: Long,
  )

  fun getAllItems(): Flow<List<Item>>
}
