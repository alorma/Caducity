package com.alorma.caducity.ui.screen.category.detail

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

data class CategoryDetailRoute(val categoryId: String) : NavKey

sealed interface CategoryDetailRoutes : NavKey {
  @Serializable
  data class Root(val categoryId: String) : CategoryDetailRoutes

  @Serializable
  data class AddInstance(
    val categoryId: String,
    val productId: String? = null
  ) : CategoryDetailRoutes
}

