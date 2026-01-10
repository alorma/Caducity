package com.alorma.caducity.ui.screen.products

import com.alorma.caducity.domain.model.ProductListItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class ProductsListMapper {
  fun mapToProductsList(
    products: ImmutableList<ProductListItem>,
  ): ImmutableList<ProductListUiModel> {
    return products
      .map { product -> toUiModel(product) }
      .toImmutableList()
  }

  private fun toUiModel(
    product: ProductListItem,
  ): ProductListUiModel {
    val allInstances = product.instances

    return if (allInstances.isEmpty()) {
      ProductListUiModel.Empty(
        id = product.product.id,
        name = product.product.name,
      )
    } else {
      val groups = allInstances
        .groupBy { instance -> instance.status }
        .mapValues { (_, instances) -> instances.size }
        .map { (status, count) ->
          ProductInstanceGroup(
            status = status,
            count = count,
          )
        }.toImmutableList()

      ProductListUiModel.WithContent(
        id = product.product.id,
        name = product.product.name,
        groups = groups,
      )
    }
  }
}
