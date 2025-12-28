package com.alorma.caducity.data.room

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

fun ProductRoomEntity.toModel(): Product {
  return Product(
    id = id,
    name = name,
    description = description,
  )
}

fun ProductInstanceRoomEntity.toModel(): ProductInstance {
  return ProductInstance(
    id = id,
    identifier = identifier,
    expirationDate = Instant.fromEpochMilliseconds(expirationDate),
  )
}

fun ProductWithInstancesRoomEntity.toModel(): ProductWithInstances {
  return ProductWithInstances(
    product = product.toModel(),
    instances = instances.map { it.toModel() }.toImmutableList(),
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
