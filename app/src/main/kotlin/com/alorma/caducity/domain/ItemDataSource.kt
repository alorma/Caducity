package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import kotlinx.coroutines.flow.Flow

interface ItemDataSource {
  suspend fun addItem(
    categoryId: String,
    item: NewItem,
  ): String

  suspend fun deleteItem(itemId: String)

  suspend fun getItem(itemId: String): Item?

  suspend fun markItemAsConsumed(itemId: String)

  /** Debug/fake-data only until the Transforma use case exists: closes the item as a transformed ingredient. */
  suspend fun markItemAsTransformed(itemId: String)

  suspend fun freezeItem(
    itemId: String,
    remainingDays: Int,
  )

  suspend fun unfreezeItem(itemId: String)

  suspend fun rescheduleItem(
    itemId: String,
    newExpirationDateMillis: Long,
  )

  suspend fun updatePackSize(
    itemId: String,
    newPackSize: Int,
  )

  fun getAllItems(): Flow<List<Item>>
}
