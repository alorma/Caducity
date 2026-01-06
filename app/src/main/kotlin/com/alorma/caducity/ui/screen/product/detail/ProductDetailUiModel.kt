package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.screen.products.ProductInstanceStatusGroup
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

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
  val instances: List<ProductInstanceDetailUiModel>,
)

@Stable
data class ProductInstanceDetailGroup(
  val identifier: String,
  val statusGroups: ImmutableList<ProductInstanceStatusGroup>,
  val frozenCount: Int,
  val instances: ImmutableList<ProductInstanceDetailUiModel>,
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