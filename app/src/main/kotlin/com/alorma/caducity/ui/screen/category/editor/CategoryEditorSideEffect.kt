package com.alorma.caducity.ui.screen.category.editor

sealed interface CategoryEditorSideEffect {
  data class ShowDeleteConfirm(
    val name: String,
    val productCount: Int,
  ) : CategoryEditorSideEffect

  data object DeleteFailed : CategoryEditorSideEffect
}
