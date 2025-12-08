package com.alorma.caducity.ui.screen.dashboard

import org.jetbrains.compose.resources.StringResource

data class DashboardSection(
  val title: StringResource,
  val itemCount: Int,
  val products: List<ProductUiModel>,
)

data class ProductUiModel(
  val id: String,
  val name: String,
  val description: String,
  val instances: List<ProductInstanceUiModel>,
)

data class ProductInstanceUiModel(
  val id: String,
  val productId: String,
  val expirationDate: String,
  val purchaseDate: String,
)
