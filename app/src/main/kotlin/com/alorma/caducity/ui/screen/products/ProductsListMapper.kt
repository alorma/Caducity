package com.alorma.caducity.ui.screen.products

import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime

class ProductsListMapper(
  private val appClock: AppClock,
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

    val today = appClock.now()
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    return ProductsListUiModel.WithInstances(
      id = product.id,
      name = product.name,
      description = product.description,
      instances = instances
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
        .sortedWith(instanceComparator)
        .toImmutableList()
    )
  }

  companion object {
    private val instanceComparator = compareBy<ProductsListInstanceUiModel>(
      // First: Sort by status priority (Expired first, then ExpiringSoon, then Fresh)
      { instance ->
        when (instance.status) {
          InstanceStatus.Expired -> 0
          InstanceStatus.ExpiringSoon -> 1
          InstanceStatus.Fresh -> 2
          InstanceStatus.Frozen -> 3
          InstanceStatus.Consumed -> 4 // Consumed items should be filtered out
        }
      },
      // Second: Sort by expiration date (earliest first)
      { instance -> instance.expirationDate }
    )
  }
}
