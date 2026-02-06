package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.CategoryWithItemsRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import com.alorma.caducity.domain.model.Product

/**
 * Facade mapper that delegates to specialized mappers.
 *
 * This class provides a unified interface for mapping between Room entities and domain models
 * while delegating to specialized mappers that each handle their own responsibilities.
 *
 * Architecture:
 * - Delegates category mapping to CategoryRoomMapper
 * - Delegates product mapping to ProductRoomMapper
 * - Delegates item mapping to ItemRoomMapper (with status calculation)
 * - Delegates composite mapping to CategoryWithItemsRoomMapper
 *
 * Usage:
 * - Inject this mapper into your data source classes
 * - Use the map* methods for all entity-to-model and model-to-entity conversions
 *
 * Dependencies:
 * - CategoryRoomMapper: Category entity <-> model
 * - ProductRoomMapper: Product entity <-> model
 * - ItemRoomMapper: Item entity <-> model (with status calculation)
 * - CategoryWithItemsRoomMapper: Composite entity <-> model
 */
class RoomEntityMapper(
  private val categoryMapper: CategoryRoomMapper,
  private val productMapper: ProductRoomMapper,
  private val itemMapper: ItemRoomMapper,
  private val categoryWithItemsMapper: CategoryWithItemsRoomMapper,
) {

  // ========== Room Entity -> Domain Model ==========

  /**
   * Maps ItemRoomEntity to Item domain model with status calculation
   */
  fun mapItemToModel(entity: ItemRoomEntity): Item {
    return itemMapper.toModel(entity)
  }

  /**
   * Maps ProductRoomEntity to Product domain model
   */
  fun mapProductToModel(entity: ProductRoomEntity): Product {
    return productMapper.toModel(entity)
  }

  // ========== Domain Model -> Room Entity ==========

  /**
   * Maps Item domain model to ItemRoomEntity
   *
   * @param model The Item domain model to convert
   * @param categoryId The category ID this item belongs to
   */
  fun mapItemToEntity(model: Item, categoryId: String): ItemRoomEntity {
    return itemMapper.toEntity(model, categoryId)
  }

  /**
   * Maps NewItem to ItemRoomEntity
   *
   * Used when creating a new item that doesn't have an ID yet.
   *
   * @param model The NewItem to convert
   * @param id The generated ID for this new item
   * @param categoryId The category ID this item belongs to
   */
  fun mapNewItemToEntity(model: NewItem, id: String, categoryId: String): ItemRoomEntity {
    return itemMapper.toEntity(model, id, categoryId)
  }

  /**
   * Maps Product domain model to ProductRoomEntity
   */
  fun mapProductToEntity(model: Product): ProductRoomEntity {
    return productMapper.toEntity(model)
  }
}
