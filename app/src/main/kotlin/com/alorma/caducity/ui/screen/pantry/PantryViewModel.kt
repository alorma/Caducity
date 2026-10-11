package com.alorma.caducity.ui.screen.pantry

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObservePantryUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToCategoryFromPantryAction
import com.alorma.caducity.feature.tracking.NavigateToNewCategoryFromPantryAction
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class PantryViewModel(
  private val observePantry: ObservePantryUseCase,
  private val eventTracker: EventTracker,
) : BaseViewModel<PantryNavigation, PantryNavigationSideEffect, Unit>() {
  private val query = MutableStateFlow("")

  @OptIn(ExperimentalCoroutinesApi::class)
  private val results =
    query.flatMapLatest { text ->
      if (text.isBlank()) flowOf(emptyList()) else observePantry.search(text)
    }

  val state: StateFlow<PantryState> =
    combine(observePantry.observe(), query, results) { data, text, found ->
      PantryState.Ready(data = data, query = text, results = found) as PantryState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PantryState.Loading)

  fun onQueryChange(text: String) {
    query.value = text
  }

  override fun navigate(navigation: PantryNavigation) {
    when (navigation) {
      is PantryNavigation.Category -> {
        eventTracker.trackAction(NavigateToCategoryFromPantryAction(navigation.source))
        emitNavigationSideEffect(PantryNavigationSideEffect.OpenCategory(navigation.categoryId))
      }
      PantryNavigation.NewCategory -> {
        eventTracker.trackAction(NavigateToNewCategoryFromPantryAction())
        emitNavigationSideEffect(PantryNavigationSideEffect.OpenNewCategory)
      }
    }
  }
}
