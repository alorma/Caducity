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
  /** Own margin picked by the user (or stored); `null` follows the default and is saved as `null`. */
  val soonDays: Int? = null,
  val defaultSoonDays: Int = 3,
  val error: CategoryEditorError? = null,
  val saving: Boolean = false,
) {
  /** What the slider shows: the own margin or, when untouched, the default. */
  val shownSoonDays: Int get() = soonDays ?: defaultSoonDays
  val isEditing: Boolean get() = categoryId != null
  val canSave: Boolean get() = !loading && name.isNotBlank() && error != CategoryEditorError.Duplicate && !saving
}
