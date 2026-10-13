package com.alorma.caducity.ui.screen.settings.margins

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.SuggestedDurations
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.usecase.CategoryDurationsUseCase
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** «Marge per categoria»: every category with its usual duration, edited in place, one row open at a time. */
class CategoryMarginsViewModel(
  pantryDataSource: PantryDataSource,
  private val durations: CategoryDurationsUseCase,
) : BaseViewModel<Unit, Unit, CategoryMarginsSideEffect>() {
  private val expandedId = MutableStateFlow<String?>(null)

  // Rows keep their place while one is open, so the row being edited does not jump under the finger.
  private var frozenOrder: List<String>? = null

  val state: StateFlow<CategoryMarginsState> =
    combine(
      pantryDataSource.observeCategories(),
      expandedId,
    ) { categories, expanded ->
      val rows =
        categories.map { category ->
          CategoryMarginRow(
            category = category,
            usualDays = category.usualDurationDays,
            suggestedDays = SuggestedDurations.forName(category.name),
          )
        }
      val order = frozenOrder.takeIf { expanded != null }
      val sorted =
        if (order != null) {
          rows.sortedBy { row -> order.indexOf(row.category.id).takeIf { it >= 0 } ?: Int.MAX_VALUE }
        } else {
          rows.sortedWith(compareBy({ it.usualDays ?: Int.MAX_VALUE }, { it.category.name.normalizeForSearch() }))
        }
      CategoryMarginsState.Ready(sorted, expanded) as CategoryMarginsState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryMarginsState.Loading)

  override fun navigate(navigation: Unit) = Unit

  fun onRowClick(categoryId: String) {
    val ready = state.value as? CategoryMarginsState.Ready ?: return
    if (expandedId.value == categoryId) {
      frozenOrder = null
      expandedId.value = null
    } else {
      frozenOrder = ready.rows.map { it.category.id }
      expandedId.value = categoryId
    }
  }

  fun onDaysChange(
    categoryId: String,
    days: Int,
  ) {
    viewModelScope.launch {
      durations.set(categoryId, days).onFailure { emitSideEffect(CategoryMarginsSideEffect.Failed) }
    }
  }

  fun onRestoreClick() {
    emitSideEffect(CategoryMarginsSideEffect.ShowRestoreConfirm)
  }

  fun onRestoreConfirmed() {
    viewModelScope.launch {
      frozenOrder = null
      expandedId.update { null }
      durations
        .restoreSuggested()
        .onSuccess { emitSideEffect(CategoryMarginsSideEffect.Restored) }
        .onFailure { emitSideEffect(CategoryMarginsSideEffect.Failed) }
    }
  }
}
