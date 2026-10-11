package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryDefaults
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException

/** Creates the starter categories on a fresh install; does nothing when categories already exist. */
class SeedCategoriesUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  @OptIn(ExperimentalUuidApi::class)
  suspend fun seed(names: List<String>): Result<Int> =
    try {
      if (pantryDataSource.getCategories().isNotEmpty()) {
        Result.success(0)
      } else {
        val categories =
          names.mapIndexed { index, name ->
            val defaults = CategoryDefaults.forName(name, index)
            Category(
              id = Uuid.random().toString(),
              name = name,
              description = "",
              shape = defaults.shape,
              icon = defaults.icon,
              sortOrder = index,
            )
          }
        pantryDataSource.createCategories(categories)
        Result.success(categories.size)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
