package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.normalizeForSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * Active, non-frozen units grouped by local expiration date within [range], optionally for one category.
 * Frozen units have no effective date; consumed and discarded ones are not active. Grouping is in memory
 * (a few hundred rows) so it does not depend on SQL and time zones.
 */
class ObserveCalendarUseCase(
  private val observeItems: ObserveItemsUseCase,
) {
  fun observe(
    range: ClosedRange<LocalDate>,
    categoryId: String? = null,
  ): Flow<Map<LocalDate, List<PantryEntry>>> =
    observeItems.observe().map { entries ->
      entries
        .filter { it.status != ExpiryStatus.FROZEN }
        .filter { categoryId == null || it.categoryId == categoryId }
        .filter { it.expiration in range }
        .groupBy { it.expiration }
        .toSortedMap()
        .mapValues { (_, day) -> day.sortedWith(compareBy({ it.name.normalizeForSearch() }, { it.itemId })) }
    }
}
