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
    if (instances.isEmpty()) {
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
      groups = groups.map { group ->
        // Separate frozen items from others
        val frozenCount = group.instances.count { it.status == com.alorma.caducity.domain.model.InstanceStatus.Frozen }

        // Group non-frozen instances by status and count them
        val statusGroups = group.instances
          .filter { it.status != com.alorma.caducity.domain.model.InstanceStatus.Frozen }
          .groupBy { it.status }
          .map { (status, instances) ->
            ProductInstanceStatusGroup(
              status = status,
              count = instances.size,
            )
          }
          .toImmutableList()

        ProductInstanceGroupUiModel(
          identifier = group.identifier,
          statusGroups = statusGroups,
          frozenCount = frozenCount,
        )
      }.toImmutableList()
    )
  }
}
