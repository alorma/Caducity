package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.CategoryListItem
import com.alorma.caducity.domain.model.ItemComparator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObtainCategoriesUseCase(
  private val categoryDataSource: CategoryDataSource,
  private val itemComparator: ItemComparator,
) {

  fun obtain(filter: ProductsListFilter): Flow<ImmutableList<CategoryListItem>> {
    return categoryDataSource.getCategories(filter).map { categories ->
      categories.map { categoryWithItems ->
        CategoryListItem(
          category = categoryWithItems.category,
          items = categoryWithItems
            .allItems
            .sortedWith(itemComparator)
            .toImmutableList(),
        )
      }.toImmutableList()
    }
  }

}