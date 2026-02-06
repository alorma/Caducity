package com.alorma.caducity.data.datasource.room.mapper

import com.alorma.caducity.data.datasource.room.model.CategoryWithItemsRoomEntity
import com.alorma.caducity.domain.model.CategoryProduct
import com.alorma.caducity.domain.model.CategoryWithItems
import kotlinx.collections.immutable.toImmutableList

/**
 * Composite mapper for CategoryWithItems entity -> domain model conversions.
 *
 * Responsibilities:
 * - Map CategoryWithItemsRoomEntity to CategoryWithItems domain model
 * - Orchestrate mapping of nested entities using specialized mappers
 * - Group items by product and handle standalone items
 *
 * Dependencies:
 * - CategoryRoomMapper: Maps category entity
 * - ProductRoomMapper: Maps product entities
 * - ItemRoomMapper: Maps item entities with status calculation
 */
class CategoryWithItemsRoomMapper(
  private val categoryMapper: CategoryRoomMapper,
  private val productMapper: ProductRoomMapper,
  private val itemMapper: ItemRoomMapper,
) {

  /**
   * Maps CategoryWithItemsRoomEntity to CategoryWithItems domain model
   *
   * This handles the complex mapping of a category with its products and items,
   * including grouping items by product and handling standalone items.
   */
  fun toModel(entity: CategoryWithItemsRoomEntity): CategoryWithItems {
    val itemsModel = entity.items.map { itemMapper.toModel(it) }

    // Group items by product
    val itemsByProduct = itemsModel
      .filter { it.productId != null }
      .groupBy { it.productId!! }

    // Include ALL products, even those with no items
    val productsWithItems = entity.products.map { productEntity ->
      val productItems = itemsByProduct[productEntity.id] ?: emptyList()
      CategoryProduct(
        product = productMapper.toModel(productEntity),
        items = productItems.toImmutableList()
      )
    }.toImmutableList()

    // Get standalone items (no productId)
    val standaloneItemsModel = itemsModel
      .filter { it.productId == null }
      .toImmutableList()

    return CategoryWithItems(
      category = categoryMapper.toModel(entity.category),
      products = productsWithItems,
      standaloneItems = standaloneItemsModel,
    )
  }
}
