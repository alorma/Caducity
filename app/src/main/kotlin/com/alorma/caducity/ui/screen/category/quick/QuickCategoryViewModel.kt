package com.alorma.caducity.ui.screen.category.quick

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.CategoryDefaults
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.QuickCategoryCloseAction
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuickCategoryViewModel(
  private val createCategory: CreateCategoryUseCase,
  private val pantryDataSource: PantryDataSource,
  private val createdEvents: CategoryCreatedEvents,
  private val eventTracker: EventTracker,
) : BaseViewModel<QuickCategoryNavigation, QuickCategoryNavigationSideEffect, Unit>() {
  private val _state = MutableStateFlow(QuickCategoryState())
  val state: StateFlow<QuickCategoryState> = _state.asStateFlow()

  private var shapePicked = false
  private var iconPicked = false

  override fun navigate(navigation: QuickCategoryNavigation) {
    when (navigation) {
      QuickCategoryNavigation.Close -> {
        eventTracker.trackAction(QuickCategoryCloseAction())
        emitNavigationSideEffect(QuickCategoryNavigationSideEffect.Close)
      }
    }
  }

  /** Until the user picks a shape or an icon, they follow the typed name (`Dairy` → glass, etc.). */
  fun onNameChange(name: String) {
    _state.update { it.copy(name = name, error = null) }
    if (shapePicked && iconPicked) return
    viewModelScope.launch {
      val defaults = CategoryDefaults.forName(name, pantryDataSource.getCategories().size)
      _state.update {
        it.copy(
          shape = if (shapePicked) it.shape else defaults.shape,
          icon = if (iconPicked) it.icon else defaults.icon,
        )
      }
    }
  }

  fun onShapeChange(shape: CategoryShape) {
    shapePicked = true
    _state.update { it.copy(shape = shape) }
  }

  fun onIconChange(icon: CategoryIcon) {
    iconPicked = true
    _state.update { it.copy(icon = icon) }
  }

  /** «Crea i fes-la servir»: creates it, hands the id back and closes the sheet. */
  fun onCreate() {
    val current = _state.value
    if (!current.canCreate) return
    _state.update { it.copy(saving = true, error = null) }
    viewModelScope.launch {
      createCategory
        .createCategory(name = current.name, description = "", shape = current.shape, icon = current.icon)
        .onSuccess { id ->
          createdEvents.emit(id)
          _state.update { it.copy(saving = false) }
          navigate(QuickCategoryNavigation.Close)
        }.onFailure { error ->
          val reason =
            if (error is PantryError.DuplicateCategoryName) QuickCategoryError.Duplicate else QuickCategoryError.Failed
          _state.update { it.copy(saving = false, error = reason) }
        }
    }
  }
}
