package com.alorma.caducity.ui.screen.pantry

import com.alorma.caducity.domain.model.PantryData
import com.alorma.caducity.domain.model.PantrySearchResult
import com.alorma.caducity.ui.components.feedback.snackbar.SnackbarRequest

sealed interface PantryState {
  data object Loading : PantryState

  data class Ready(
    val data: PantryData,
    val query: String,
    /** Only meaningful while [query] is not blank. */
    val results: List<PantrySearchResult>,
  ) : PantryState {
    val isSearching: Boolean get() = query.isNotBlank()
  }
}

sealed interface PantryNavigation {
  data class Category(
    val categoryId: String,
    val source: String,
  ) : PantryNavigation

  data object NewCategory : PantryNavigation
}

sealed interface PantryNavigationSideEffect {
  data class OpenCategory(
    val categoryId: String,
  ) : PantryNavigationSideEffect

  data object OpenNewCategory : PantryNavigationSideEffect
}

sealed interface PantrySideEffect {
  data class ShowSnackbar(
    val request: SnackbarRequest,
  ) : PantrySideEffect
}
