package com.alorma.caducity.ui.screen.status

import androidx.compose.runtime.Immutable
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import kotlinx.datetime.LocalDate

/** One row of the list: the units of a product that share a date (frozen: the day they were frozen). */
@Immutable
data class StatusRow(
  val key: String,
  val name: String,
  val category: Category,
  val expiration: LocalDate,
  val frozenSince: LocalDate?,
  /** Closest to expire first (see `sortedNearestFirst`): the row's round buttons act on [nearest]. */
  val entries: List<PantryEntry>,
) {
  val nearest: PantryEntry get() = entries.first()
  val itemIds: List<String> get() = entries.map { it.itemId }
  val unitCount: Int get() = entries.size
}

@Immutable
data class StatusSection(
  val category: Category,
  val rows: List<StatusRow>,
) {
  val unitCount: Int get() = rows.sumOf { it.unitCount }
}

sealed interface StatusListState {
  data object Loading : StatusListState

  @Immutable
  data class Ready(
    val status: ExpiryStatus,
    val totalCount: Int,
    val sections: List<StatusSection>,
    val selecting: Boolean,
    val selectedKeys: Set<String>,
  ) : StatusListState {
    val rows: List<StatusRow> get() = sections.flatMap { it.rows }
    val selectedRows: List<StatusRow> get() = rows.filter { it.key in selectedKeys }
    val selectedUnitCount: Int get() = selectedRows.sumOf { it.unitCount }
    val isEmpty: Boolean get() = totalCount == 0
  }
}

/** What a date picked in the quick-dates sheet is for: the units of the selected rows (bulk reschedule). */
@Immutable
data class DateTarget(
  val itemIds: List<String>,
  val names: List<String>,
)

sealed interface StatusListNavigation {
  data class ItemActions(
    val itemIds: List<String>,
  ) : StatusListNavigation

  data object AddFirst : StatusListNavigation

  /** Turns the units of the selected rows into new products. */
  data object TransformSelection : StatusListNavigation
}

sealed interface StatusListNavigationSideEffect {
  data class OpenItemActions(
    val itemIds: List<String>,
  ) : StatusListNavigationSideEffect

  data object OpenAdd : StatusListNavigationSideEffect

  data class OpenTransform(
    val itemIds: List<String>,
  ) : StatusListNavigationSideEffect
}

sealed interface StatusListSideEffect {
  data class ShowDiscardConfirm(
    val itemIds: List<String>,
    val unitCount: Int,
  ) : StatusListSideEffect

  data class ShowQuickDates(
    val target: DateTarget,
  ) : StatusListSideEffect
}
