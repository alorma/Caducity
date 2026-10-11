package com.alorma.caducity.ui.screen.category.detail

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface CategoryDetailRoutes : NavKey {
  @Serializable
  data class Root(
    val categoryId: String,
  ) : CategoryDetailRoutes
}
