package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.Category
import kotlinx.coroutines.flow.Flow

/** Categories ordered by `sortOrder`; re-emits on any change. */
class ObserveCategoriesUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  fun observe(): Flow<List<Category>> = pantryDataSource.observeCategories()
}
