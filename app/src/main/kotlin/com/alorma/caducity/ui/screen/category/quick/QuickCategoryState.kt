package com.alorma.caducity.ui.screen.category.quick

import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape

enum class QuickCategoryError {
  Duplicate,
  Failed,
}

data class QuickCategoryState(
  val name: String = "",
  val shape: CategoryShape = CategoryShape.COOKIE,
  val icon: CategoryIcon = CategoryIcon.DEFAULT,
  val error: QuickCategoryError? = null,
  val saving: Boolean = false,
) {
  val canCreate: Boolean get() = name.isNotBlank() && !saving
}
