package com.alorma.caducity.data.room

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.time.clock.AppClock
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
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
  return ProductInstance(
    id = id,
    identifier = identifier,
    expirationDate = expirationInstant,
    status = InstanceStatus.calculateStatus(
      expirationDate = expirationInstant,
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold
    ),
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
  )
}
