package com.alorma.caducity.ui.screen.products

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

sealed class ProductsListState {
  data object Loading : ProductsListState()

  data class Success(
    val items: ImmutableList<ProductsListUiModel>,
  ) : ProductsListState()
}

@Stable
sealed interface ProductsListUiModel {
  val id: String
  val name: String
  val description: String

  @Stable
  data class WithInstances(
    override val id: String,
    override val name: String,
    override val description: String,
    val instances: ImmutableList<ProductsListInstanceUiModel>,
  ) : ProductsListUiModel

  @Stable
  data class Empty(
    override val id: String,
    override val name: String,
    override val description: String,
  ) : ProductsListUiModel
}

@Stable
data class ProductsListInstanceUiModel(
  val id: String,
  val identifier: String,
  val statusText: String,
  val statusColor: Long,
  val expirationDate: LocalDate,
  val expirationDateText: String,
)
