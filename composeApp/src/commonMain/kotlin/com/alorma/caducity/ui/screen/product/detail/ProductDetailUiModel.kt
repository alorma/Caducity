package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.runtime.Stable
import com.alorma.caducity.base.main.InstanceStatus
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

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
  val expirationDateText: String,
  val expirationInstant: Instant,
)
