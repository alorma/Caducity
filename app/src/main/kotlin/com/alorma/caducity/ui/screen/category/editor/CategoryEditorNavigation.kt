package com.alorma.caducity.ui.screen.category.editor

sealed interface CategoryEditorNavigation {
  data object Close : CategoryEditorNavigation
}

sealed interface CategoryEditorNavigationSideEffect {
  data object Close : CategoryEditorNavigationSideEffect

  /** A new category was created: the app opens it. */
  data class Created(
    val categoryId: String,
  ) : CategoryEditorNavigationSideEffect

  /** The category being edited was deleted, so the screen under the editor is gone too. */
  data object Deleted : CategoryEditorNavigationSideEffect
}
