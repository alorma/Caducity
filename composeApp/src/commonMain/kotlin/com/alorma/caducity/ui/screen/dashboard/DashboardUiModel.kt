package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate

enum class SectionType {
  EXPIRED,
  EXPIRING_SOON,
  FRESH,
  EMPTY,
}

@Stable
sealed interface ProductUiModel {
  val id: String
  val name: String
  val description: String

  @Stable
  data class WithInstances(
    override val id: String,
    override val name: String,
    override val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val today: LocalDate,
    val instances: List<ProductInstanceUiModel>,
  ) : ProductUiModel

  @Stable
  data class Empty(
    override val id: String,
    override val name: String,
    override val description: String,
  ) : ProductUiModel
}

@Stable
data class ProductInstanceUiModel(
  val id: String,
  val expirationDate: LocalDate,
)
