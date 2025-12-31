package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ObtainDashboardProductsUseCase(
  private val productDataSource: ProductDataSource,
) {

  fun obtainProducts(
    filter: ProductsListFilter,
  ): Flow<ImmutableList<ProductWithInstances>> {
    return productDataSource
      .products
      .map { products ->
        products
          .filter { product -> matchesFilter(product, filter) }
          .sortedBy { product ->
            product.instances.minOfOrNull { instance -> instance.expirationDate }
          }
          .toImmutableList()
      }
  }

  private fun matchesFilter(product: ProductWithInstances, filter: ProductsListFilter): Boolean {
    return when (filter) {
      is ProductsListFilter.All -> true
      is ProductsListFilter.ByDate -> {
        product.instances.any { instance ->
          val expirationDate = instance.expirationDate
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
          expirationDate == filter.date
        }
      }
      is ProductsListFilter.ByDateRange -> {
        product.instances.any { instance ->
          val expirationDate = instance.expirationDate
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
          expirationDate >= filter.startDate && expirationDate <= filter.endDate
        }
      }
      is ProductsListFilter.ByStatus -> {
        product.instances.any { instance ->
          instance.status in filter.statuses
        }
      }
    }
  }
}
