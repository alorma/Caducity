package com.alorma.caducity.ui.screen.product.detail

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

data class ProductDetailRoute(val productId: String) : NavKey

sealed interface ProductDetailRoutes : NavKey {
  @Serializable
  data class Root(val productId: String) : ProductDetailRoutes

  @Serializable
  data class AddInstance(val productId: String) : ProductDetailRoutes
}

