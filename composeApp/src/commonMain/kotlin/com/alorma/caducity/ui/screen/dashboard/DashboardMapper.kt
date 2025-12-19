package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.time.clock.AppClock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DashboardMapper(
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {
  fun mapToDashboardSections(
    products: List<ProductWithInstances>,
    searchQuery: String = "",
    statusFilters: Set<InstanceStatus> = emptySet(),
  ): List<ProductUiModel> {
    return products
      .map { it.toUiModel() }
      .filter { product ->
        matchesSearchQuery(product, searchQuery) && matchesStatusFilters(product, statusFilters)
      }
  }

  private fun matchesSearchQuery(product: ProductUiModel, query: String): Boolean {
    if (query.isBlank()) return true
    val lowerQuery = query.lowercase()
    return product.name.lowercase().contains(lowerQuery) ||
      product.description.lowercase().contains(lowerQuery)
  }

  private fun matchesStatusFilters(product: ProductUiModel, filters: Set<InstanceStatus>): Boolean {
    if (filters.isEmpty()) return true
    return when (product) {
      is ProductUiModel.Empty -> filters.isEmpty()
      is ProductUiModel.WithInstances -> {
        product.instances.any { instance ->
          filters.contains(instance.status)
        }
      }
    }
  }

  private fun ProductWithInstances.toUiModel(): ProductUiModel {
    if (instances.isEmpty()) {
      return ProductUiModel.Empty(
        id = product.id,
        name = product.name,
        description = product.description,
      )
    }

    val now = appClock.now()

    val today = now
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    val expiringSoonDate = now
      .plus(expirationThresholds.soonExpiringThreshold)
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    return ProductUiModel.WithInstances(
      id = product.id,
      name = product.name,
      description = product.description,
      today = today,
      instances = instances.map { instance ->
        val expirationDate = instance
          .expirationDate
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date

        ProductInstanceUiModel(
          id = instance.id,
          identifier = instance.identifier,
          status = if (expirationDate < today) {
            InstanceStatus.Expired
          } else if (expirationDate > today && expirationDate < expiringSoonDate) {
            InstanceStatus.ExpiringSoon
          } else {
            InstanceStatus.Fresh
          },
          expirationDate = expirationDate,
        )
      }
    )
  }
}
