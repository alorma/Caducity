package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.model.ItemStatus

sealed interface DashboardNavigation {
  data object CreateCategory : DashboardNavigation

  data class Category(
    val categoryId: String,
    val source: String,
  ) : DashboardNavigation

  data class FilteredItems(
    val status: ItemStatus,
  ) : DashboardNavigation

  data object Settings : DashboardNavigation

  data object AiAssistant : DashboardNavigation
}

sealed interface DashboardNavigationSideEffect {
  data object NavigateToCreateCategory : DashboardNavigationSideEffect

  data class NavigateToCategory(
    val categoryId: String,
  ) : DashboardNavigationSideEffect

  data class NavigateToFilteredItems(
    val status: ItemStatus,
  ) : DashboardNavigationSideEffect

  data object NavigateToSettings : DashboardNavigationSideEffect

  data object NavigateToAiAssistant : DashboardNavigationSideEffect
}
