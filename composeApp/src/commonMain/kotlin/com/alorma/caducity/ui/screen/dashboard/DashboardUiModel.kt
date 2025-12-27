package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate

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
    val today: String,
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
  val identifier: String,
  val status: InstanceStatus,
  val expirationDate: String,
)

sealed class InstanceStatus {
  data object Expired : InstanceStatus()
  data object ExpiringSoon : InstanceStatus()
  data object Fresh : InstanceStatus()
}