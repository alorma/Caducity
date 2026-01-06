package com.alorma.caducity.ui.screen.products

import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductListItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat

class ProductsListMapper(
  private val dateFormat: DateTimeFormat<LocalDate>,
) {
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
    return if (product.variants.isEmpty() && product.standaloneInstances.isEmpty()) {
      ProductListUiModel.Empty(
        id = product.product.id,
        name = product.product.name
      )
    } else {

      val standaloneInstances = product
        .standaloneInstances
        .map { instance ->
          ProductListStandaloneInstance(
            name = instance.identifier,
            status = instance.status,
          )
        }
        .toImmutableList()

      val variants = product
        .variants
        .map { variant ->
          val groups = variant.instances.groupBy { it.status }

          val statusGroups = groups
            .filter { (status, instances) ->
              status != InstanceStatus.Frozen && instances.isNotEmpty()
            }
            .map { (status, instances) ->
              ProductInstanceVariantGroup(
                status = status,
                count = instances.size,
              )
            }.toImmutableList()

          ProductInstanceVariant(
            id = variant.variant.id,
            name = variant.variant.name,
            statusGroups = statusGroups,
            frozenCount = groups[InstanceStatus.Frozen]?.size ?: 0,
          )
        }

      ProductListUiModel.WithContent(
        id = product.product.id,
        name = product.product.name,
        variants = persistentListOf(),
        standaloneInstances = persistentListOf(),
      )
    }
  }
}
