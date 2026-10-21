package com.alorma.caducity.ui.screen.status

import androidx.compose.runtime.Immutable
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.ui.components.feedback.snackbar.SnackbarRequest
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
  val productCount: Int get() = rows.distinctBy { it.nearest.groupKey }.size
}

sealed interface StatusListState {
  data object Loading : StatusListState

  @Immutable
  data class Ready(
    val status: ExpiryStatus,
    val totalCount: Int,
    val sections: List<StatusSection>,
    /** The global "Soon" window, shown in the Aviat subtitle. */
    val soonDays: Int,
  ) : StatusListState {
    val rows: List<StatusRow> get() = sections.flatMap { it.rows }
    val isEmpty: Boolean get() = totalCount == 0
  }
}

sealed interface StatusListNavigation {
  data class ItemActions(
    val itemIds: List<String>,
  ) : StatusListNavigation

  data object AddFirst : StatusListNavigation
}

sealed interface StatusListNavigationSideEffect {
  data class OpenItemActions(
    val itemIds: List<String>,
  ) : StatusListNavigationSideEffect

  data object OpenAdd : StatusListNavigationSideEffect
}

sealed interface StatusListSideEffect {
  data class ShowDiscardConfirm(
    val itemIds: List<String>,
    val productCount: Int,
  ) : StatusListSideEffect

  data class ShowSnackbar(
    val request: SnackbarRequest,
  ) : StatusListSideEffect
}
