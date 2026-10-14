package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryCategory
import com.alorma.caducity.domain.model.PantryData
import com.alorma.caducity.domain.model.PantrySearchResult
import com.alorma.caducity.domain.model.STATUS_ORDER
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.model.productCount
import com.alorma.caducity.domain.model.toProductGroups
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Rebost: every category with its counters (in `sortOrder`) and the product search (P11). */
class ObservePantryUseCase(
  private val observeItems: ObserveItemsUseCase,
  private val pantryDataSource: PantryDataSource,
  private val todayProvider: TodayProvider,
) {
  fun observe(): Flow<PantryData> =
    combine(
      observeItems.observe(),
      pantryDataSource.observeCategories(),
      todayProvider.observe(),
    ) { entries, categories, today ->
      val byCategory = entries.groupBy { it.categoryId }
      PantryData(
        today = today,
        totalProducts = entries.productCount(),
        categories =
          categories.map { category ->
            val units = byCategory[category.id].orEmpty()
            val counts = units.groupBy { it.status }.mapValues { (_, inStatus) -> inStatus.productCount() }
            PantryCategory(
              category = category,
              productCount = units.productCount(),
              expiredCount = counts[ExpiryStatus.EXPIRED] ?: 0,
              statusCounts = STATUS_ORDER.mapNotNull { status -> counts[status]?.let { status to it } }.toMap(),
              nearest = units.filter { it.status != ExpiryStatus.FROZEN }.minOfOrNull { it.expiration },
            )
          },
      )
    }

  /** Active products whose name or category contains the text, ignoring case and accents. */
  fun search(query: String): Flow<List<PantrySearchResult>> =
    combine(observeItems.observe(), pantryDataSource.observeCategories()) { entries, categories ->
      val text = query.normalizeForSearch()
      if (text.isEmpty()) {
        emptyList()
      } else {
        val categoriesById = categories.associateBy { it.id }
        entries
          .toProductGroups()
          .mapNotNull { group ->
            val category = categoriesById[group.categoryId] ?: return@mapNotNull null
            val matches =
              group.name.normalizeForSearch().contains(text) || category.name.normalizeForSearch().contains(text)
            if (matches) PantrySearchResult(group, category) else null
          }
      }
    }
}
