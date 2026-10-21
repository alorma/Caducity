package com.alorma.caducity.ui.screen.category.editor

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryDefaults
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.UpdateCategoryUseCase
import com.alorma.caducity.feature.tracking.CategoryDeletedAction
import com.alorma.caducity.feature.tracking.CategoryEditorCloseAction
import com.alorma.caducity.feature.tracking.CategorySavedAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** «Nova categoria» (`categoryId == null`) and «Edita la categoria». */
class CategoryEditorViewModel(
  private val categoryId: String?,
  private val pantryDataSource: PantryDataSource,
  private val createCategory: CreateCategoryUseCase,
  private val updateCategory: UpdateCategoryUseCase,
  private val deleteCategory: DeleteCategoryUseCase,
  private val eventTracker: EventTracker,
) : BaseViewModel<CategoryEditorNavigation, CategoryEditorNavigationSideEffect, CategoryEditorSideEffect>() {
  private val _state = MutableStateFlow(CategoryEditorState(categoryId = categoryId))
  val state: StateFlow<CategoryEditorState> = _state.asStateFlow()

  private var categories: List<Category> = emptyList()
  private var shapePicked = categoryId != null
  private var iconPicked = categoryId != null

  private val mode: String get() = if (categoryId == null) "create" else "edit"

  init {
    viewModelScope.launch {
      categories = pantryDataSource.getCategories()
      val current = categories.firstOrNull { it.id == categoryId }
      _state.update {
        if (current == null) {
          it.copy(loading = false)
        } else {
          it.copy(
            loading = false,
            usualDurationDays = current.usualDurationDays,
            canFreeze = current.canFreeze,
            name = current.name,
            description = current.description,
            shape = current.shape,
            icon = current.icon,
          )
        }
      }
    }
  }

  override fun navigate(navigation: CategoryEditorNavigation) {
    when (navigation) {
      CategoryEditorNavigation.Close -> {
        eventTracker.trackAction(CategoryEditorCloseAction(mode))
        emitNavigationSideEffect(CategoryEditorNavigationSideEffect.Close)
      }
    }
  }

  /** While creating, shape and icon follow the typed name until the user picks one. */
  fun onNameChange(name: String) {
    val defaults = CategoryDefaults.forName(name, categories.size)
    _state.update {
      it.copy(
        name = name,
        error = if (isDuplicate(name)) CategoryEditorError.Duplicate else null,
        shape = if (shapePicked) it.shape else defaults.shape,
        icon = if (iconPicked) it.icon else defaults.icon,
      )
    }
  }

  fun onDescriptionChange(description: String) {
    _state.update { it.copy(description = description) }
  }

  fun onShapeChange(shape: CategoryShape) {
    shapePicked = true
    _state.update { it.copy(shape = shape) }
  }

  fun onIconChange(icon: CategoryIcon) {
    iconPicked = true
    _state.update { it.copy(icon = icon) }
  }

  /** Touching the slider sets the category's usual duration. */
  fun onUsualDurationChange(days: Int) {
    _state.update { it.copy(usualDurationDays = days) }
  }

  fun onCanFreezeChange(canFreeze: Boolean) {
    _state.update { it.copy(canFreeze = canFreeze) }
  }

  fun onSave() {
    val current = _state.value
    if (!current.canSave) return
    _state.update { it.copy(saving = true, error = null) }
    viewModelScope.launch {
      val result =
        if (categoryId == null) {
          createCategory
            .createCategory(
              name = current.name,
              description = current.description.trim(),
              shape = current.shape,
              icon = current.icon,
              usualDurationDays = current.usualDurationDays,
              canFreeze = current.canFreeze,
            ).map { id -> CategoryEditorNavigationSideEffect.Created(id) }
        } else {
          updateCategory
            .update(
              categoryId = categoryId,
              name = current.name,
              shape = current.shape,
              icon = current.icon,
              usualDurationDays = current.usualDurationDays,
              canFreeze = current.canFreeze,
              description = current.description,
            ).map { CategoryEditorNavigationSideEffect.Close }
        }
      result
        .onSuccess { effect ->
          eventTracker.trackAction(CategorySavedAction(mode))
          _state.update { it.copy(saving = false) }
          emitNavigationSideEffect(effect)
        }.onFailure { error ->
          val reason =
            if (error is PantryError.DuplicateCategoryName) {
              CategoryEditorError.Duplicate
            } else {
              CategoryEditorError.Failed
            }
          _state.update { it.copy(saving = false, error = reason) }
        }
    }
  }

  fun onDeleteClick() {
    val id = categoryId ?: return
    val name = categories.firstOrNull { it.id == id }?.name ?: _state.value.name
    viewModelScope.launch {
      val productCount =
        pantryDataSource
          .observeActiveItems()
          .first()
          .filter { it.categoryId == id }
          .distinctBy { it.productId ?: it.name.normalizeForSearch() }
          .size
      emitSideEffect(CategoryEditorSideEffect.ShowDeleteConfirm(name, productCount))
    }
  }

  fun onDeleteConfirmed() {
    val id = categoryId ?: return
    viewModelScope.launch {
      deleteCategory
        .deleteCategory(id)
        .onSuccess {
          eventTracker.trackAction(CategoryDeletedAction())
          emitNavigationSideEffect(CategoryEditorNavigationSideEffect.Deleted)
        }.onFailure { emitSideEffect(CategoryEditorSideEffect.DeleteFailed) }
    }
  }

  private fun isDuplicate(name: String): Boolean {
    val normalized = name.trim().normalizeForSearch()
    return normalized.isNotEmpty() &&
      categories.any { it.id != categoryId && it.name.normalizeForSearch() == normalized }
  }
}
