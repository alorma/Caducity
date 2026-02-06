package com.alorma.caducity.data.datasource.room.mapper

import com.alorma.caducity.data.datasource.room.ProductRoomEntity
import com.alorma.caducity.domain.model.Product
import kotlin.time.Instant

/**
 * Mapper for Product entity <-> domain model conversions.
 *
 * Responsibilities:
 * - Map ProductRoomEntity to Product domain model
 * - Map Product domain model to ProductRoomEntity
 * - Handle timestamp conversions for createdAt field
 */
class ProductRoomMapper {

  /**
   * Maps ProductRoomEntity to Product domain model
   */
  fun toModel(entity: ProductRoomEntity): Product {
    return Product(
      id = entity.id,
      categoryId = entity.categoryId,
      name = entity.name,
      createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
    )
  }

  /**
   * Maps Product domain model to ProductRoomEntity
   */
  fun toEntity(model: Product): ProductRoomEntity {
    return ProductRoomEntity(
      id = model.id,
      categoryId = model.categoryId,
      name = model.name,
      createdAt = model.createdAt.toEpochMilliseconds(),
    )
  }
}
