package com.alorma.caducity.ui.screen.home

import com.alorma.caducity.domain.model.HomeData
import kotlinx.datetime.LocalDate

sealed interface HomeState {
  data object Loading : HomeState

  data class Ready(
    val data: HomeData,
    val widgetTipVisible: Boolean,
  ) : HomeState
}

sealed interface HomeNavigation {
  /** [date] opens the month view with that day selected (P18). */
  data class Calendar(
    val date: LocalDate? = null,
    val source: String = "button",
  ) : HomeNavigation

  data object Search : HomeNavigation

  data object Expired : HomeNavigation

  data class Pantry(
    val source: String,
  ) : HomeNavigation

  data class ItemActions(
    val itemId: String,
  ) : HomeNavigation

  data object AddFirst : HomeNavigation
}

sealed interface HomeNavigationSideEffect {
  data class OpenCalendar(
    val date: LocalDate?,
  ) : HomeNavigationSideEffect

  data object OpenSearch : HomeNavigationSideEffect

  data object OpenExpired : HomeNavigationSideEffect

  data object OpenPantry : HomeNavigationSideEffect

  data class OpenItemActions(
    val itemId: String,
  ) : HomeNavigationSideEffect

  data object OpenAdd : HomeNavigationSideEffect
}
