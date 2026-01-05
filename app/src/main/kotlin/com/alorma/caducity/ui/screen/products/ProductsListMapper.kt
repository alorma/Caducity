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
        ProductInstanceGroupUiModel(
          identifier = group.identifier,
          instances = group.instances
            .map { instance ->
              // Use displayDate for frozen items (pausedDate) or expirationDate for others
              val displayLocalDate = instance
                .displayDate
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date

              val status = instance.status

              ProductsListInstanceUiModel(
                id = instance.id,
                identifier = instance.identifier,
                status = status,
                expirationDate = displayLocalDate,
                expirationDateText = dateFormat.format(displayLocalDate),
              )
            }
            .toImmutableList()
        )
      }.toImmutableList()
    )
  }
}
