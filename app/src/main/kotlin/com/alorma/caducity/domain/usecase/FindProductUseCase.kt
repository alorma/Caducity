package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ProductUsage
import com.alorma.caducity.domain.model.normalizeForSearch

/** The existing product named [name] in a category, with its own «es pot congelar» override and unit type. */
class FindProductUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  /** `null` when the product does not exist. */
  suspend fun find(
    categoryId: String,
    name: String,
  ): ProductUsage? {
    val typed = name.normalizeForSearch()
    if (typed.isEmpty()) return null
    return pantryDataSource
      .getProductUsage()
      .firstOrNull { it.categoryId == categoryId && it.name.normalizeForSearch() == typed }
  }
}
