package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

enum class SectionType {
  EXPIRED,
  EXPIRING_SOON,
  FRESH,
  EMPTY,
}

@Stable
data class DashboardSection(
  val type: SectionType,
  val itemCount: Int,
  val products: List<ProductUiModel>,
)

@Stable
data class ProductUiModel(
  val id: String,
  val name: String,
  val description: String,
  val startDate: LocalDate,
  val endDate: LocalDate,
  val instances: List<ProductInstanceUiModel>,
)

@Stable
data class ProductInstanceUiModel(
  val id: String,
  val productId: String,
  val expirationDate: String,
  val expirationDateInstant: Instant,
  val purchaseDate: String,
  val purchaseDateInstant: Instant,
)
