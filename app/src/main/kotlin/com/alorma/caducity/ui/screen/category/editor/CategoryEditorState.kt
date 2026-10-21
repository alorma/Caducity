package com.alorma.caducity.ui.screen.category.editor

import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape

enum class CategoryEditorError {
  Duplicate,
  Failed,
}

/** Slider position shown while a category has no usual duration yet. */
private const val DEFAULT_SHOWN_DURATION_DAYS = 7

data class CategoryEditorState(
  /** `null` when creating a category. */
  val categoryId: String? = null,
  val loading: Boolean = true,
  val name: String = "",
  val description: String = "",
  val shape: CategoryShape = CategoryShape.COOKIE,
  val icon: CategoryIcon = CategoryIcon.DEFAULT,
  /** Usual duration picked by the user (or stored); `null` is saved as `null` (no duration). */
  val usualDurationDays: Int? = null,
  /** Whether the category's products can be frozen (on by default). */
  val canFreeze: Boolean = true,
  val error: CategoryEditorError? = null,
  val saving: Boolean = false,
) {
  /** What the slider shows: the stored duration or, when there is none, [DEFAULT_SHOWN_DURATION_DAYS]. */
  val shownDurationDays: Int get() = usualDurationDays ?: DEFAULT_SHOWN_DURATION_DAYS
  val isEditing: Boolean get() = categoryId != null
  val canSave: Boolean get() = !loading && name.isNotBlank() && error != CategoryEditorError.Duplicate && !saving
}
