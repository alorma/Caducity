package com.alorma.caducity.ui.screen.products

import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime

class ProductsListMapper(
  private val dateFormat: DateTimeFormat<LocalDate>,
) {
  fun mapToProductsList(
    products: ImmutableList<ProductWithInstances>,
  ): ImmutableList<ProductsListUiModel> {
    return products.map { it.toUiModel() }.toImmutableList()
  }

  private fun ProductWithInstances.toUiModel(): ProductsListUiModel {
    val allInstances = variants.flatMap { it.instances } + standaloneInstances

    if (allInstances.isEmpty()) {
      return ProductsListUiModel.Empty(
        id = product.id,
        name = product.name,
        description = product.description,
      )
    }

    return ProductsListUiModel.WithInstances(
      id = product.id,
      name = product.name,
      description = product.description,
      groups = variants.map { variantWithInstances ->
        val instances = variantWithInstances.instances

        // Separate frozen items from others
        val frozenCount = instances.count { it.status == com.alorma.caducity.domain.model.InstanceStatus.Frozen }

        // Group non-frozen instances by status and count them
        val statusGroups = instances
          .filter { it.status != com.alorma.caducity.domain.model.InstanceStatus.Frozen }
          .groupBy { it.status }
          .map { (status, instancesList) ->
            ProductInstanceStatusGroup(
              status = status,
              count = instancesList.size,
            )
          }
          .toImmutableList()

        ProductInstanceGroupUiModel(
          identifier = variantWithInstances.variant.name,
          statusGroups = statusGroups,
          frozenCount = frozenCount,
        )
      }.toImmutableList()
    )
  }
}
