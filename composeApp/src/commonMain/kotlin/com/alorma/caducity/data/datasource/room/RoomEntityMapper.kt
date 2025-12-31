package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.time.clock.AppClock
import com.alorma.caducity.base.main.InstanceStatus
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

fun ProductRoomEntity.toModel(): Product {
  return Product(
    id = id,
    name = name,
    description = description,
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
    expirationDate = expirationInstant,
    status = status,
    pausedDate = pausedInstant,
  )
}

fun ProductWithInstancesRoomEntity.toModel(
  appClock: AppClock,
  expirationThresholds: ExpirationThresholds
): ProductWithInstances {
  return ProductWithInstances(
    product = product.toModel(),
    instances = instances.map { it.toModel(appClock, expirationThresholds) }.toImmutableList(),
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
