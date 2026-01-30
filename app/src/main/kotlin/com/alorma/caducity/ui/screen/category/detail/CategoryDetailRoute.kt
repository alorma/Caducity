package com.alorma.caducity.ui.screen.category.detail

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

data class CategoryDetailRoute(val productId: String) : NavKey

sealed interface CategoryDetailRoutes : NavKey {
  @Serializable
  data class Root(val productId: String) : CategoryDetailRoutes

  @Serializable
  data class AddInstance(
    val productId: String,
    val variantId: String? = null
  ) : CategoryDetailRoutes
}

