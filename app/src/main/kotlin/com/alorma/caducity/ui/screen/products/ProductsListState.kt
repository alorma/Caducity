package com.alorma.caducity.ui.screen.products

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.usecase.ProductsListFilter
import kotlinx.collections.immutable.ImmutableList

sealed class ProductsListState {
  data object Loading : ProductsListState()

  data class Success(
    val items: ImmutableList<ProductListUiModel>,
  ) : ProductsListState()

  data class Empty(
    val filter: ProductsListFilter,
  ) : ProductsListState()
}

@Stable
sealed interface ProductListUiModel {
  val id: String
  val name: String

  @Stable
  data class WithContent(
    override val id: String,
    override val name: String,
    val variants: ImmutableList<ProductInstanceVariant>,
    val standaloneInstances: ImmutableList<ProductListStandaloneInstance>,
  ) : ProductListUiModel

  @Stable
  data class Empty(
    override val id: String,
    override val name: String,
  ) : ProductListUiModel
}

@Stable
data class ProductInstanceVariant(
  val id: String,
  val name: String,
  val statusGroups: ImmutableList<ProductInstanceVariantGroup>,
  val frozenCount: Int,
)

@Stable
data class ProductInstanceVariantGroup(
  val status: InstanceStatus,
  val count: Int,
)

@Stable
data class ProductListStandaloneInstance(
  val id: String,
  val name: String,
  val status: InstanceStatus,
)