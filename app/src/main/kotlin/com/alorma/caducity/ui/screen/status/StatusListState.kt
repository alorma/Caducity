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
  /** Closest to expire first: the row's buttons act on [nearest]. */
  val entries: List<PantryEntry>,
) {
  val nearest: PantryEntry get() = entries.first()
  val itemIds: List<String> get() = entries.map { it.itemId }
  val unitCount: Int get() = entries.size
  val legacyPack: Int? get() = if (entries.size == 1) nearest.legacyPack else null
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
    val today: LocalDate,
    val totalCount: Int,
    val sections: List<StatusSection>,
    /** «Aviat» threshold in days, for the help text. */
    val soonDays: Int,
    val selecting: Boolean,
    val selectedKeys: Set<String>,
  ) : StatusListState {
    val rows: List<StatusRow> get() = sections.flatMap { it.rows }
    val selectedRows: List<StatusRow> get() = rows.filter { it.key in selectedKeys }
    val selectedUnitCount: Int get() = selectedRows.sumOf { it.unitCount }
    val isEmpty: Boolean get() = totalCount == 0
  }
}

/** What a date picked in the quick-dates sheet is for. */
@Immutable
data class DateTarget(
  val itemIds: List<String>,
  val names: List<String>,
  /** Thawing: the date is the new expiration of a frozen unit. */
  val unfreeze: Boolean,
  /** Date proposed when thawing (today + the days that were left). */
  val proposed: LocalDate?,
)

sealed interface StatusListNavigation {
  data class ItemActions(
    val itemId: String,
  ) : StatusListNavigation

  data object AddFirst : StatusListNavigation
}

sealed interface StatusListNavigationSideEffect {
  data class OpenItemActions(
    val itemId: String,
  ) : StatusListNavigationSideEffect

  data object OpenAdd : StatusListNavigationSideEffect
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
