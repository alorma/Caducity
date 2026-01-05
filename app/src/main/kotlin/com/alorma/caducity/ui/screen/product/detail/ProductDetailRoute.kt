package com.alorma.caducity.ui.screen.product.detail

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ProductDetailRoute : NavKey {
  val productId: String

  @Serializable
  data class Root(override val productId: String) : ProductDetailRoute
}
