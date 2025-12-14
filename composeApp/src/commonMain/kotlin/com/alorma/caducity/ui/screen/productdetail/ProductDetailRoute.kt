package com.alorma.caducity.ui.screen.productdetail

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailRoute(val productId: String) : NavKey
