package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface CategoryDataSource {
  fun getCategories(): Flow<ImmutableList<CategoryWithItems>>

  fun getCategory(categoryId: String): Flow<Result<CategoryWithItems>>

  suspend fun createCategory(
    category: Category,
    items: ImmutableList<Item>,
  )

  suspend fun deleteCategory(categoryId: String)
}
