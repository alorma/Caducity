package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ActiveItem
import kotlinx.coroutines.flow.first

/** One active unit with its product name, to prefill the edit form. */
class GetActiveItemUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun get(itemId: String): ActiveItem? = all().firstOrNull { it.itemId == itemId }

  suspend fun all(): List<ActiveItem> = pantryDataSource.observeActiveItems().first()
}
