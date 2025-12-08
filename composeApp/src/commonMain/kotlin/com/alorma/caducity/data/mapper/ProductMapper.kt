package com.alorma.caducity.data.mapper

import com.alorma.caducity.data.entity.ProductEntity
import com.alorma.caducity.data.entity.ProductInstanceEntity
import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import kotlin.time.Instant

fun ProductEntity.toDomain(): Product = Product(
  id = id,
  name = name,
  description = description,
)

fun Product.toEntity(): ProductEntity = ProductEntity(
  id = id,
  name = name,
  description = description,
)

fun ProductInstanceEntity.toDomain(): ProductInstance = ProductInstance(
  id = id,
  productId = productId,
  expirationDate = Instant.fromEpochMilliseconds(expirationDate),
  purchaseDate = Instant.fromEpochMilliseconds(purchaseDate),
)

fun ProductInstance.toEntity(): ProductInstanceEntity = ProductInstanceEntity(
  id = id,
  productId = productId,
  expirationDate = expirationDate.toEpochMilliseconds(),
  purchaseDate = purchaseDate.toEpochMilliseconds(),
)
