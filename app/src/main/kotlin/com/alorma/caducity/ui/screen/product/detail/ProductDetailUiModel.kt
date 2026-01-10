package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.InstanceStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Stable
data class DateInstancesUiModel(
  val text: String,
  val status: InstanceStatus,
  val date: LocalDate,
  val instances: ImmutableList<ProductInstanceDetailUiModel>,
)

@Stable
data class ProductInstanceDetailUiModel(
  val id: String,
  val expirationDate: LocalDate,
  val status: InstanceStatus,
  val text: String,
)

@Stable
data class ProductDetailUiModel(
  val id: String,
  val name: String,
  val description: String,
)

@Stable
data class ProductDetailVariantUiModel(
  val id: String,
  val name: String,
  val instances: List<LegacyProductInstanceDetailUiModel>,
)

@Stable
data class LegacyProductInstanceDetailUiModel(
  val id: String,
  val identifier: String,
  val status: InstanceStatus,
  val expirationDate: LocalDate,
  val expirationDateText: String,
  val expirationInstant: Instant,
)