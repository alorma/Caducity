package com.alorma.caducity.data.datasource.room.mapper

/**
 * Composite mapper for CategoryWithItems entity -> domain model conversions.
 *
 * Responsibilities:
 * - Map CategoryWithItemsRoomEntity to CategoryWithItems domain model
 * - Orchestrate mapping of nested entities using specialized mappers
 * - Group items by product and handle standalone items
 *
 * Dependencies:
 * - CategoryRoomMapper: Maps category entity
 * - ProductRoomMapper: Maps product entities
 * - ItemRoomMapper: Maps item entities with status calculation
 */
class CategoryWithItemsRoomMapper(
  private val categoryMapper: CategoryRoomMapper,
)
