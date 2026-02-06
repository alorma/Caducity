package com.alorma.caducity.data.datasource.room.mapper

import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.domain.model.Category

/**
 * Mapper for Category entity <-> domain model conversions.
 *
 * Responsibilities:
 * - Map CategoryRoomEntity to Category domain model
 * - Map Category domain model to CategoryRoomEntity
 */
class CategoryRoomMapper {

  /**
   * Maps CategoryRoomEntity to Category domain model
   */
  fun toModel(entity: CategoryRoomEntity): Category {
    return Category(
      id = entity.id,
      name = entity.name,
      description = entity.description,
    )
  }

  /**
   * Maps Category domain model to CategoryRoomEntity
   */
  fun toEntity(model: Category): CategoryRoomEntity {
    return CategoryRoomEntity(
      id = model.id,
      name = model.name,
      description = model.description,
    )
  }
}
