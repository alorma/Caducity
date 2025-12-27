package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.runtime.Stable
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import kotlinx.datetime.LocalDate

@Stable
data class ProductDetailUiModel(
  val id: String,
  val name: String,
  val description: String,
  val instances: List<ProductInstanceDetailUiModel>,
)

@Stable
data class ProductInstanceDetailUiModel(
  val id: String,
  val identifier: String,
  val status: InstanceStatus,
  val expirationDate: LocalDate,
)
