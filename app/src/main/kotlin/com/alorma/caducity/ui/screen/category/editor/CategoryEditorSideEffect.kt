package com.alorma.caducity.ui.screen.category.editor

sealed interface CategoryEditorSideEffect {
  data class ShowDeleteConfirm(
    val name: String,
    val unitCount: Int,
  ) : CategoryEditorSideEffect

  data object DeleteFailed : CategoryEditorSideEffect
}
