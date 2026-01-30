package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.CategoryWithItems
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

class ObtainDashboardCategoriesUseCase(
  private val categoryDataSource: CategoryDataSource,
) {

  fun obtainCategories(): Flow<ImmutableList<CategoryWithItems>> {
    return categoryDataSource.getCategories(
      filter = ProductsListFilter.All
    )
  }
}
