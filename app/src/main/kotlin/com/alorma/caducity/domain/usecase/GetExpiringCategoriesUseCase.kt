package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first

/**
 * Use case for getting categories that are expiring soon or expired.
 * Filters categories based on the item status (ExpiringSoon or Expired).
 */
class GetExpiringCategoriesUseCase(
  private val categoryDataSource: CategoryDataSource,
) {

  /**
   * Returns a list of categories that are expiring soon or already expired.
   * Only includes categories with items that have ExpiringSoon or Expired status.
   */
  suspend fun load(): List<CategoryWithItems> {
    // Get all categories filtered by expiring/expired status
    val statusFilter = ProductsListFilter.ByStatus(
      statuses = setOf(ItemStatus.ExpiringSoon, ItemStatus.Expired)
    )
    val filteredCategories = categoryDataSource.getCategories(statusFilter).first()

    return filteredCategories
      .map { categoryWithItems ->
        // For each category, only include items that are expiring or expired
        val filteredProducts = categoryWithItems.products.map { productWithItems ->
          productWithItems.copy(
            items = productWithItems.items.filter { item ->
              item.status == ItemStatus.ExpiringSoon || item.status == ItemStatus.Expired
            }.toImmutableList()
          )
        }.filter { it.items.isNotEmpty() }.toImmutableList()

        val filteredStandaloneItems = categoryWithItems.standaloneItems.filter { item ->
          item.status == ItemStatus.ExpiringSoon || item.status == ItemStatus.Expired
        }.toImmutableList()

        categoryWithItems.copy(
          products = filteredProducts,
          standaloneItems = filteredStandaloneItems
        )
      }
      .sortedBy { categoryWithItems ->
        // Sort by earliest expiration date across all items
        val allItems = categoryWithItems.products.flatMap { it.items } +
                         categoryWithItems.standaloneItems
        allItems.minOfOrNull { it.expirationDate }
      }
  }
}
