package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.CategoryData
import com.alorma.caducity.domain.model.CategorySection
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.STATUS_ORDER
import com.alorma.caducity.domain.model.toProductGroups
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** One category: counters per status and its products grouped by status (Expired → Soon → Fresh → Frozen). */
class ObserveCategoryUseCase(
  private val observeItems: ObserveItemsUseCase,
  private val pantryDataSource: PantryDataSource,
  private val todayProvider: TodayProvider,
) {
  /** Emits `null` when the category does not exist (e.g. it was deleted). */
  fun observe(categoryId: String): Flow<CategoryData?> =
    combine(
      observeItems.observe(),
      pantryDataSource.observeCategories(),
      todayProvider.observe(),
    ) { entries, categories, today ->
      val category = categories.firstOrNull { it.id == categoryId } ?: return@combine null
      val units = entries.filter { it.categoryId == categoryId }
      val counts = units.groupingBy { it.status }.eachCount()
      CategoryData(
        today = today,
        category = category,
        unitCount = units.size,
        nearest = units.filter { it.status != ExpiryStatus.FROZEN }.minOfOrNull { it.expiration },
        statusCounts = STATUS_ORDER.mapNotNull { status -> counts[status]?.let { status to it } }.toMap(),
        sections =
          STATUS_ORDER.mapNotNull { status ->
            val groups = units.filter { it.status == status }.toProductGroups()
            if (groups.isEmpty()) null else CategorySection(status, groups)
          },
      )
    }
}
