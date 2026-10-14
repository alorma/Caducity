package com.alorma.caducity.ui.screen.home

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.HomeData
import com.alorma.caducity.domain.usecase.WeekStripData
import kotlinx.datetime.LocalDate

sealed interface HomeState {
  data object Loading : HomeState

  data class Ready(
    val data: HomeData,
    val weekStrip: WeekStripData,
  ) : HomeState
}

sealed interface HomeNavigation {
  /** [date] opens the month view with that day selected (P18). */
  data class Calendar(
    val date: LocalDate? = null,
    val source: String = "button",
  ) : HomeNavigation

  data object Search : HomeNavigation

  /** The banner (`source = banner`) and the summary cells (`source = summary`) open the status lists. */
  data class StatusList(
    val status: ExpiryStatus,
    val source: String,
  ) : HomeNavigation

  data class Pantry(
    val source: String,
  ) : HomeNavigation

  data class ItemActions(
    val itemIds: List<String>,
  ) : HomeNavigation

  data object AddFirst : HomeNavigation

  /** The «Has cuinat?» card opens an empty Transforma. */
  data class Transform(
    val source: String,
  ) : HomeNavigation
}

sealed interface HomeNavigationSideEffect {
  data class OpenCalendar(
    val date: LocalDate?,
  ) : HomeNavigationSideEffect

  data object OpenSearch : HomeNavigationSideEffect

  data class OpenStatusList(
    val status: ExpiryStatus,
  ) : HomeNavigationSideEffect

  data object OpenPantry : HomeNavigationSideEffect

  data class OpenItemActions(
    val itemIds: List<String>,
  ) : HomeNavigationSideEffect

  data object OpenAdd : HomeNavigationSideEffect

  data object OpenTransform : HomeNavigationSideEffect
}
