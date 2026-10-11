package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.SeedCategory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException

/** Creates the starter categories on a fresh install; does nothing when categories already exist. */
class SeedCategoriesUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  @OptIn(ExperimentalUuidApi::class)
  suspend fun seed(seeds: List<SeedCategory>): Result<Int> =
    try {
      if (pantryDataSource.getCategories().isNotEmpty()) {
        Result.success(0)
      } else {
        val categories =
          seeds.mapIndexed { index, seed ->
            Category(
              id = Uuid.random().toString(),
              name = seed.name,
              description = "",
              shape = seed.shape,
              icon = seed.icon,
              sortOrder = index,
              soonThresholdDays = seed.soonThresholdDays,
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
