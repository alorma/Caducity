package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Stable
import org.jetbrains.compose.resources.StringResource

@Stable
data class DashboardSection(
  val title: StringResource,
  val itemCount: Int,
  val products: List<ProductUiModel>,
)

@Stable
data class ProductUiModel(
  val id: String,
  val name: String,
  val description: String,
  val instances: List<ProductInstanceUiModel>,
)

@Stable
data class ProductInstanceUiModel(
  val id: String,
  val productId: String,
  val expirationDate: String,
  val purchaseDate: String,
)
