package com.alorma.caducity.ui.screen.category.editor

import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape

enum class CategoryEditorError {
  Duplicate,
  Failed,
}

data class CategoryEditorState(
  /** `null` when creating a category. */
  val categoryId: String? = null,
  val loading: Boolean = true,
  val name: String = "",
  val description: String = "",
  val shape: CategoryShape = CategoryShape.COOKIE,
  val icon: CategoryIcon = CategoryIcon.DEFAULT,
  val error: CategoryEditorError? = null,
  val saving: Boolean = false,
) {
  val isEditing: Boolean get() = categoryId != null
  val canSave: Boolean get() = !loading && name.isNotBlank() && error != CategoryEditorError.Duplicate && !saving
}
