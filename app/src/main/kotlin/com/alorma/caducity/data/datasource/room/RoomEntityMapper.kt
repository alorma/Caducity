package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryProduct
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

/**
 * Mapper class for converting between Room entities and domain models.
 *
 * This class provides a testable way to map data between the data layer (Room entities)
 * and the domain layer (domain models). All mapping logic is centralized here to ensure
 * consistency and ease of testing.
 *
 * Usage:
 * - Inject this mapper into your data source classes
 * - Use the map* methods for all entity-to-model and model-to-entity conversions
 *
 * @param appClock Clock instance for getting current time
 * @param expirationThresholds Thresholds for calculating item expiration status
 */
class RoomEntityMapper(
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {

  // ========== Room Entity -> Domain Model ==========

  /**
   * Maps CategoryRoomEntity to Category domain model
   */
  fun mapCategoryToModel(entity: CategoryRoomEntity): Category {
    return Category(
      id = entity.id,
      name = entity.name,
      description = entity.description,
    )
  }

  /**
   * Maps ItemRoomEntity to Item domain model
   *
   * Uses ItemRoomMapper for consistent status calculation.
   */
  fun mapItemToModel(entity: ItemRoomEntity): Item {
    val expirationInstant = Instant.fromEpochMilliseconds(entity.expirationDate)
    val pausedInstant = entity.pausedDate?.let { Instant.fromEpochMilliseconds(it) }

    // Determine status using ItemRoomMapper for consistency
    val itemRoomMapper = com.alorma.caducity.data.datasource.ItemRoomMapper(
      appClock = appClock,
      expirationThresholds = expirationThresholds
    )
    val status = itemRoomMapper.toModel(entity).status

    return Item(
      id = entity.id,
      identifier = entity.identifier,
      productId = entity.productId,
      expirationDate = expirationInstant,
      status = status,
      pausedDate = pausedInstant,
    )
  }

  /**
   * Maps ProductRoomEntity to Product domain model
   */
  fun mapProductToModel(entity: ProductRoomEntity): Product {
    return Product(
      id = entity.id,
      categoryId = entity.categoryId,
      name = entity.name,
      createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
    )
  }

  /**
   * Maps CategoryWithItemsRoomEntity to CategoryWithItems domain model
   *
   * This handles the complex mapping of a category with its products and items,
   * including grouping items by product and handling standalone items.
   */
  fun mapCategoryWithItemsToModel(entity: CategoryWithItemsRoomEntity): CategoryWithItems {
    val itemsModel = entity.items.map { mapItemToModel(it) }

    // Group items by product
    val itemsByProduct = itemsModel
      .filter { it.productId != null }
      .groupBy { it.productId!! }

    // Include ALL products, even those with no items
    val productsWithItems = entity.products.map { productEntity ->
      val productItems = itemsByProduct[productEntity.id] ?: emptyList()
      CategoryProduct(
        product = mapProductToModel(productEntity),
        items = productItems.toImmutableList()
      )
    }.toImmutableList()

    // Get standalone items (no productId)
    val standaloneItemsModel = itemsModel
      .filter { it.productId == null }
      .toImmutableList()

    return CategoryWithItems(
      category = mapCategoryToModel(entity.category),
      products = productsWithItems,
      standaloneItems = standaloneItemsModel,
    )
  }

  // ========== Domain Model -> Room Entity ==========

  /**
   * Maps Category domain model to CategoryRoomEntity
   */
  fun mapCategoryToEntity(model: Category): CategoryRoomEntity {
    return CategoryRoomEntity(
      id = model.id,
      name = model.name,
      description = model.description,
    )
  }

  /**
   * Maps Item domain model to ItemRoomEntity
   *
   * @param model The Item domain model to convert
   * @param categoryId The category ID this item belongs to
   */
  fun mapItemToEntity(model: Item, categoryId: String): ItemRoomEntity {
    return ItemRoomEntity(
      id = model.id,
      categoryId = categoryId,
      identifier = model.identifier,
      productId = model.productId,
      expirationDate = model.expirationDate.toEpochMilliseconds(),
      pausedDate = model.pausedDate?.toEpochMilliseconds(),
      remainingDays = null,
      consumedDate = null,
    )
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
    return ItemRoomEntity(
      id = id,
      categoryId = categoryId,
      identifier = model.identifier,
      productId = model.productId,
      expirationDate = model.expirationDate.toEpochMilliseconds(),
      pausedDate = null,
      remainingDays = null,
      consumedDate = null,
    )
  }

  /**
   * Maps Product domain model to ProductRoomEntity
   */
  fun mapProductToEntity(model: Product): ProductRoomEntity {
    return ProductRoomEntity(
      id = model.id,
      categoryId = model.categoryId,
      name = model.name,
      createdAt = model.createdAt.toEpochMilliseconds(),
    )
  }
}
