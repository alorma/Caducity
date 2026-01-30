package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryProduct
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.NewItem
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

// CategoryRoomEntity maps to Category domain model
fun CategoryRoomEntity.toModel(): Category {
  return Category(
    id = id,
    name = name,
    description = description,
  )
}

// NewItem maps to ItemRoomEntity
fun NewItem.toRoomEntity(id: String, categoryId: String): ItemRoomEntity {
  return ItemRoomEntity(
    id = id,
    categoryId = categoryId,
    identifier = this.identifier,
    productId = this.productId,
    expirationDate = expirationDate.toEpochMilliseconds(),
    pausedDate = null,
    remainingDays = null,
    consumedDate = null,
  )
}

// ItemRoomEntity maps to Item domain model
@Deprecated("Use mapper")
fun ItemRoomEntity.toModel(
  appClock: AppClock,
  expirationThresholds: ExpirationThresholds
): Item {
  val expirationInstant = Instant.fromEpochMilliseconds(expirationDate)
  val pausedInstant = pausedDate?.let { Instant.fromEpochMilliseconds(it) }

  // Determine status: consumed > frozen > calculated
  val status = when {
    pausedDate != null -> ItemStatus.Frozen
    else -> ItemStatus.calculateStatus(
      expirationDate = expirationInstant,
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold
    )
  }

  return Item(
    id = id,
    identifier = identifier,
    categoryId = categoryId,
    expirationDate = expirationInstant,
    status = status,
    pausedDate = pausedInstant,
  )
}

// CategoryWithItemsRoomEntity maps to CategoryWithItems domain model
fun CategoryWithItemsRoomEntity.toModel(
  appClock: AppClock,
  expirationThresholds: ExpirationThresholds
): CategoryWithItems {
  val itemsModel = items.map { it.toModel(appClock, expirationThresholds) }

  // Build product map for quick lookup (ProductRoomEntity = products in domain)
  val productMap = products.associateBy { it.id }

  // Group items by product (categoryId in ItemRoomEntity = categoryId in domain)
  val itemsByProduct = itemsModel
    .filter { it.categoryId != null }
    .groupBy { it.categoryId!! }

  // Include ALL products, even those with no items (products = products in domain)
  val productsWithItems = products.map { productEntity ->
    val productItems = itemsByProduct[productEntity.id] ?: emptyList()
    CategoryProduct(
      product = productEntity.toModel(),
      items = productItems.toImmutableList()
    )
  }.toImmutableList()

  // Get standalone items (no categoryId = no categoryId in domain)
  val standaloneItemsModel = itemsModel
    .filter { it.categoryId == null }
    .toImmutableList()

  return CategoryWithItems(
    category = category.toModel(),
    products = productsWithItems,
    standaloneItems = standaloneItemsModel,
  )
}

// Category domain model maps to CategoryRoomEntity
fun Category.toRoomEntity(): CategoryRoomEntity {
  return CategoryRoomEntity(
    id = id,
    name = name,
    description = description,
  )
}

// Item domain model maps to ItemRoomEntity
fun Item.toRoomEntity(categoryId: String): ItemRoomEntity {
  return ItemRoomEntity(
    id = id,
    categoryId = categoryId,
    identifier = identifier,
    productId = productId,
    expirationDate = expirationDate.toEpochMilliseconds(),
    pausedDate = null,
    remainingDays = null,
    consumedDate = null,
  )
}

// ProductRoomEntity maps to Product domain model
fun ProductRoomEntity.toModel(): Product {
  return Product(
    id = id,
    categoryId = categoryId,
    name = name,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
  )
}

// Product domain model maps to ProductRoomEntity
fun Product.toRoomEntity(): ProductRoomEntity {
  return ProductRoomEntity(
    id = id,
    categoryId = categoryId,
    name = name,
    createdAt = createdAt.toEpochMilliseconds(),
  )
}
