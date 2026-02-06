package com.alorma.caducity.data.datasource.room.mapper

import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.CategoryWithItemsRoomEntity
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryProduct
import com.alorma.caducity.domain.model.CategoryWithItems
import kotlinx.collections.immutable.toImmutableList

/**
 * Mapper for Category entity <-> domain model conversions.
 *
 * Responsibilities:
 * - Map CategoryRoomEntity to Category domain model
 * - Map Category domain model to CategoryRoomEntity
 */
class CategoryRoomMapper(
  private val productMapper: ProductRoomMapper,
  private val itemMapper: ItemRoomMapper,
) {

  /**
   * Maps CategoryRoomEntity to Category domain model
   */
  fun toModel(entity: CategoryRoomEntity): Category {
    return Category(
      id = entity.id,
      name = entity.name,
      description = entity.description,
    )
  }

  /**
   * Maps Category domain model to CategoryRoomEntity
   */
  fun toEntity(model: Category): CategoryRoomEntity {
    return CategoryRoomEntity(
      id = model.id,
      name = model.name,
      description = model.description,
    )
  }


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
      category = toModel(entity.category),
      products = productsWithItems,
      standaloneItems = standaloneItemsModel,
    )
  }
}
