package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.normalizeForSearch

/** The «es pot congelar» override of the product named [name] in a category, if that product already exists. */
class FindProductCanFreezeUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  /** `null` when the product does not exist or follows its category. */
  suspend fun find(
    categoryId: String,
    name: String,
  ): Boolean? {
    val typed = name.normalizeForSearch()
    if (typed.isEmpty()) return null
    return pantryDataSource
      .getProductUsage()
      .firstOrNull { it.categoryId == categoryId && it.name.normalizeForSearch() == typed }
      ?.canFreeze
  }
}
