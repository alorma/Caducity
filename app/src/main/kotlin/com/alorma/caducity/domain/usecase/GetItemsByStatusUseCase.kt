package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case for getting items filtered by status across all categories.
 * Groups items by category and filters based on the provided status.
 */
class GetItemsByStatusUseCase(
  private val categoryDataSource: CategoryDataSource,
  private val itemDataSource: ItemDataSource,
) {

  /**
   * Returns a flow of categories with items filtered by the provided status.
   * Only includes categories that have items with the specified status.
   */
  fun load(status: ItemStatus): Flow<List<CategoryWithItems>> {
    return combine(
      categoryDataSource.getCategories(),
      itemDataSource.getAllItems()
    ) { categories, allItems ->
      // Filter items by status
      val filteredItemsById = allItems
        .filter { it.status == status }
        .associateBy { it.id }

      // For each category, only include items that match the status
      categories.mapNotNull { categoryWithItems ->
        val filteredProducts = categoryWithItems.products.map { productWithItems ->
          productWithItems.copy(
            items = productWithItems.items.filter { item ->
              filteredItemsById.containsKey(item.id)
            }.toImmutableList()
          )
        }.filter { it.items.isNotEmpty() }.toImmutableList()

        val filteredStandaloneItems = categoryWithItems.standaloneItems.filter { item ->
          filteredItemsById.containsKey(item.id)
        }.toImmutableList()

        // Only include category if it has items with the status
        if (filteredProducts.isNotEmpty() || filteredStandaloneItems.isNotEmpty()) {
          categoryWithItems.copy(
            products = filteredProducts,
            standaloneItems = filteredStandaloneItems
          )
        } else {
          null
        }
      }.sortedBy { categoryWithItems ->
        // Sort by category name
        categoryWithItems.category.name
      }
    }
  }
}
