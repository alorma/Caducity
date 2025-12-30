package com.alorma.caducity.ui.screen.products

import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

class ProductsListMapper(
  private val appClock: AppClock,
  private val dateFormat: DateTimeFormat<LocalDate>,
) {
  fun mapToProductsList(
    products: ImmutableList<ProductWithInstances>,
    filter: ProductsListFilter,
  ): ImmutableList<ProductsListUiModel> {
    return products.map { it.toUiModel() }.toImmutableList()
  }

  private fun matchesStatus(
    instance: ProductsListInstanceUiModel,
    status: InstanceStatus
  ): Boolean {
    val now = appClock.now()
    val today = now
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    return when (status) {
      InstanceStatus.Expired -> instance.expirationDate <= today
      InstanceStatus.ExpiringSoon -> {
        val expiringSoonDate = now
          .plus(Duration.parse("7d"))
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date
        instance.expirationDate > today && instance.expirationDate < expiringSoonDate
      }

      InstanceStatus.Fresh -> {
        val expiringSoonDate = now
          .plus(Duration.parse("7d"))
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date
        instance.expirationDate >= expiringSoonDate
      }
    }
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
      instances = instances.map { instance ->
        val expirationLocalDate = instance
          .expirationDate
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date

        val status = instance.status

        ProductsListInstanceUiModel(
          id = instance.id,
          identifier = instance.identifier,
          status = status,
          expirationDate = expirationLocalDate,
          expirationDateText = dateFormat.format(expirationLocalDate),
        )
      }.toImmutableList()
    )
  }
}
