package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.NameSuggestion
import com.alorma.caducity.domain.model.normalizeForSearch

/**
 * Product names from the history that start with or contain the typed text (no accents, no case).
 * Names starting with the text come first, then the most used, then the most recent.
 */
class GetNameSuggestionsUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun suggest(
    query: String,
    limit: Int = DEFAULT_LIMIT,
  ): List<NameSuggestion> {
    val text = query.normalizeForSearch()
    if (text.isEmpty()) return emptyList()

    return pantryDataSource
      .getProductUsage()
      .map { it to it.name.normalizeForSearch() }
      .filter { (_, name) -> name.contains(text) }
      .sortedWith(
        compareByDescending<Pair<com.alorma.caducity.domain.model.ProductUsage, String>> { (_, name) ->
          name.startsWith(text)
        }.thenByDescending { (usage, _) -> usage.uses }
          .thenByDescending { (usage, _) -> usage.lastUsed }
          .thenBy { (_, name) -> name },
      ).take(limit)
      .map { (usage, _) -> NameSuggestion(usage.productId, usage.name, usage.categoryId, usage.canFreeze) }
  }

  companion object {
    const val DEFAULT_LIMIT = 3
  }
}
