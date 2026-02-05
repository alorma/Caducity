package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface CategoryDataSource {
  fun getCategories(): Flow<ImmutableList<CategoryWithItems>>

  fun getCategory(categoryId: String): Flow<Result<CategoryWithItems>>

  suspend fun createCategory(category: Category, items: ImmutableList<Item>)

  suspend fun addItem(categoryId: String, item: NewItem): String

  suspend fun deleteItem(itemId: String)

  suspend fun deleteCategory(categoryId: String)

  suspend fun getItem(itemId: String): Item?

  suspend fun markItemAsConsumed(itemId: String)

  suspend fun freezeItem(itemId: String, remainingDays: Int)

  suspend fun unfreezeItem(itemId: String)

  suspend fun clearAllCategories()

  suspend fun clearAllItems()
}
