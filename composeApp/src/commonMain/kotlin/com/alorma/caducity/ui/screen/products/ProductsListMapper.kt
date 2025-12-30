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
    val allProducts = products.map { it.toUiModel() }

    return when (filter) {
      is ProductsListFilter.ByDate -> {
        allProducts.filter { product ->
          if (product is ProductsListUiModel.WithInstances) {
            product.instances.any { instance ->
              instance.expirationDate == filter.date
            }
          } else {
            false
          }
        }.toImmutableList()
      }

      is ProductsListFilter.ByStatus -> {
        allProducts.mapNotNull { product ->
          if (product is ProductsListUiModel.WithInstances) {
            val matchingInstances = product.instances.filter { instance ->
              filter.statuses.any { status ->
                matchesStatus(instance, status)
              }
            }.toImmutableList()

            if (matchingInstances.isNotEmpty()) {
              product.copy(instances = matchingInstances)
            } else {
              null
            }
          } else {
            null
          }
        }.toImmutableList()
      }

      is ProductsListFilter.ByDateRange -> {
        allProducts.filter { product ->
          if (product is ProductsListUiModel.WithInstances) {
            product.instances.any { instance ->
              instance.expirationDate >= filter.startDate &&
                instance.expirationDate <= filter.endDate
            }
          } else {
            false
          }
        }.toImmutableList()
      }

      ProductsListFilter.All -> allProducts.toImmutableList()
    }
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

    val now = appClock.now()

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
          statusText = when (status) {
            InstanceStatus.Expired -> "Expired"
            InstanceStatus.ExpiringSoon -> "Expiring Soon"
            InstanceStatus.Fresh -> "Fresh"
          },
          statusColor = when (status) {
            InstanceStatus.Expired -> 0xFFE53935
            InstanceStatus.ExpiringSoon -> 0xFFFB8C00
            InstanceStatus.Fresh -> 0xFF43A047
          },
          expirationDate = expirationLocalDate,
          expirationDateText = dateFormat.format(expirationLocalDate),
        )
      }.toImmutableList()
    )
  }
}
