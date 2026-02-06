package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import kotlinx.coroutines.flow.Flow

interface ItemDataSource {
  suspend fun addItem(categoryId: String, item: NewItem): String

  suspend fun deleteItem(itemId: String)

  suspend fun getItem(itemId: String): Item?

  suspend fun markItemAsConsumed(itemId: String)

  suspend fun freezeItem(itemId: String, remainingDays: Int)

  suspend fun unfreezeItem(itemId: String)

  fun getItemsByProduct(categoryId: String, productId: String?): Flow<List<Item>>

  suspend fun clearConsumedItems(categoryId: String, productId: String?)

  suspend fun clearAllItems(categoryId: String, productId: String?)
}
