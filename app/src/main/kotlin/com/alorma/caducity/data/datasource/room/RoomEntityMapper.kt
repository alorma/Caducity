package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.NewProductInstance
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductVariant
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.model.Variant
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

// CategoryRoomEntity maps to Product domain model
fun CategoryRoomEntity.toModel(): Product {
  return Product(
    id = id,
    name = name,
    description = description,
  )
}

// NewProductInstance maps to ItemRoomEntity
fun NewProductInstance.toRoomEntity(id: String, categoryId: String): ItemRoomEntity {
  return ItemRoomEntity(
    id = id,
    categoryId = categoryId,
    identifier = this.identifier,
    productId = this.variantId,
    expirationDate = expirationDate.toEpochMilliseconds(),
    pausedDate = null,
    remainingDays = null,
    consumedDate = null,
  )
}

// ItemRoomEntity maps to ProductInstance domain model
@Deprecated("Use mapper")
fun ItemRoomEntity.toModel(
  appClock: AppClock,
  expirationThresholds: ExpirationThresholds
): ProductInstance {
  val expirationInstant = Instant.fromEpochMilliseconds(expirationDate)
  val pausedInstant = pausedDate?.let { Instant.fromEpochMilliseconds(it) }

  // Determine status: consumed > frozen > calculated
  val status = when {
    pausedDate != null -> InstanceStatus.Frozen
    else -> InstanceStatus.calculateStatus(
      expirationDate = expirationInstant,
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold
    )
  }

  return ProductInstance(
    id = id,
    identifier = identifier,
    variantId = productId,
    expirationDate = expirationInstant,
    status = status,
    pausedDate = pausedInstant,
  )
}

// CategoryWithItemsRoomEntity maps to ProductWithInstances domain model
fun CategoryWithItemsRoomEntity.toModel(
  appClock: AppClock,
  expirationThresholds: ExpirationThresholds
): ProductWithInstances {
  val itemsModel = items.map { it.toModel(appClock, expirationThresholds) }

  // Build product map for quick lookup (ProductRoomEntity = variants in domain)
  val productMap = products.associateBy { it.id }

  // Group items by product (productId in ItemRoomEntity = variantId in domain)
  val itemsByProduct = itemsModel
    .filter { it.variantId != null }
    .groupBy { it.variantId!! }

  // Include ALL products, even those with no items (products = variants in domain)
  val productsWithItems = products.map { productEntity ->
    val productItems = itemsByProduct[productEntity.id] ?: emptyList()
    ProductVariant(
      variant = productEntity.toModel(),
      instances = productItems.toImmutableList()
    )
  }.toImmutableList()

  // Get standalone items (no productId = no variantId in domain)
  val standaloneItemsModel = itemsModel
    .filter { it.variantId == null }
    .toImmutableList()

  return ProductWithInstances(
    product = category.toModel(),
    variants = productsWithItems,
    standaloneInstances = standaloneItemsModel,
  )
}

// Product domain model maps to CategoryRoomEntity
fun Product.toRoomEntity(): CategoryRoomEntity {
  return CategoryRoomEntity(
    id = id,
    name = name,
    description = description,
  )
}

// ProductInstance domain model maps to ItemRoomEntity
fun ProductInstance.toRoomEntity(categoryId: String): ItemRoomEntity {
  return ItemRoomEntity(
    id = id,
    categoryId = categoryId,
    identifier = identifier,
    productId = variantId,
    expirationDate = expirationDate.toEpochMilliseconds(),
    pausedDate = null,
    remainingDays = null,
    consumedDate = null,
  )
}

// ProductRoomEntity maps to Variant domain model
fun ProductRoomEntity.toModel(): Variant {
  return Variant(
    id = id,
    productId = categoryId,
    name = name,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
  )
}

// Variant domain model maps to ProductRoomEntity
fun Variant.toRoomEntity(): ProductRoomEntity {
  return ProductRoomEntity(
    id = id,
    categoryId = productId,
    name = name,
    createdAt = createdAt.toEpochMilliseconds(),
  )
}
