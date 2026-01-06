package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.NewProductInstance
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.model.Variant
import com.alorma.caducity.domain.model.VariantWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

fun ProductRoomEntity.toModel(): Product {
  return Product(
    id = id,
    name = name,
    description = description,
  )
}

fun NewProductInstance.toRoomEntity(id: String, productId: String): ProductInstanceRoomEntity {
  return ProductInstanceRoomEntity(
    id = id,
    productId = productId,
    identifier = this.identifier,
    variantId = this.variantId,
    expirationDate = expirationDate.toEpochMilliseconds(),
    pausedDate = null,
    remainingDays = null,
    consumedDate = null,
  )
}

fun ProductInstanceRoomEntity.toModel(
  appClock: AppClock,
  expirationThresholds: ExpirationThresholds
): ProductInstance {
  val expirationInstant = Instant.fromEpochMilliseconds(expirationDate)
  val pausedInstant = pausedDate?.let { Instant.fromEpochMilliseconds(it) }

  // Determine status: consumed > frozen > calculated
  val status = when {
    consumedDate != null -> InstanceStatus.Consumed
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
    variantId = variantId,
    expirationDate = expirationInstant,
    status = status,
    pausedDate = pausedInstant,
  )
}

fun ProductWithInstancesRoomEntity.toModel(
  appClock: AppClock,
  expirationThresholds: ExpirationThresholds
): ProductWithInstances {
  val instancesModel = instances.map { it.toModel(appClock, expirationThresholds) }

  // Build variant map for quick lookup
  val variantMap = variants.associateBy { it.id }

  // Group instances by variant
  val variantsWithInstances = instancesModel
    .filter { it.variantId != null }
    .groupBy { it.variantId!! }
    .mapNotNull { (variantId, variantInstances) ->
      val variant = variantMap[variantId] ?: return@mapNotNull null
      VariantWithInstances(
        variant = variant.toModel(),
        instances = variantInstances.toImmutableList()
      )
    }
    .toImmutableList()

  // Get standalone instances (no variantId)
  val standaloneInstancesModel = instancesModel
    .filter { it.variantId == null }
    .toImmutableList()

  return ProductWithInstances(
    product = product.toModel(),
    variants = variantsWithInstances,
    standaloneInstances = standaloneInstancesModel,
  )
}

fun Product.toRoomEntity(): ProductRoomEntity {
  return ProductRoomEntity(
    id = id,
    name = name,
    description = description,
  )
}

fun ProductInstance.toRoomEntity(productId: String): ProductInstanceRoomEntity {
  return ProductInstanceRoomEntity(
    id = id,
    productId = productId,
    identifier = identifier,
    expirationDate = expirationDate.toEpochMilliseconds(),
    pausedDate = null,
    remainingDays = null,
    consumedDate = null,
  )
}

fun VariantRoomEntity.toModel(): Variant {
  return Variant(
    id = id,
    productId = productId,
    name = name,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
  )
}

fun Variant.toRoomEntity(): VariantRoomEntity {
  return VariantRoomEntity(
    id = id,
    productId = productId,
    name = name,
    createdAt = createdAt.toEpochMilliseconds(),
  )
}
