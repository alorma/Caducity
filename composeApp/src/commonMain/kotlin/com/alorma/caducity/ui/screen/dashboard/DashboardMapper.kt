package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime

class DashboardMapper(
  private val appClock: AppClock,
  private val dateFormat: DateTimeFormat<LocalDate>,
) {
  fun mapToDashboardSections(
    products: ImmutableList<ProductWithInstances>,
    searchQuery: String = "",
    statusFilters: Set<InstanceStatus> = emptySet(),
  ): ImmutableList<ProductUiModel> {
    return products
      .map { it.toUiModel() }
      .filter { product ->
        matchesSearchQuery(product, searchQuery) && matchesStatusFilters(product, statusFilters)
      }.toImmutableList()
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
      is ProductUiModel.Empty -> false  // Empty products don't match any status filter
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

    return ProductUiModel.WithInstances(
      id = product.id,
      name = product.name,
      description = product.description,
      today = dateFormat.format(today),
      instances = instances.map { instance ->
        val expirationLocalDate = instance
          .expirationDate
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date

        ProductInstanceUiModel(
          id = instance.id,
          identifier = instance.identifier,
          status = instance.status,
          expirationDate = instance.expirationDate
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date,
          expirationDateText = dateFormat.format(expirationLocalDate),
        )
      }.toImmutableList()
    )
  }
}
