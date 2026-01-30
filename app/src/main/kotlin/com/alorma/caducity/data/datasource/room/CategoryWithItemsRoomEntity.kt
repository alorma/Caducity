package com.alorma.caducity.data.datasource.room

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithItemsRoomEntity(
  @Embedded
  val category: CategoryRoomEntity,

  @Relation(
    parentColumn = "id",
    entityColumn = "categoryId"
  )
  val items: List<ItemRoomEntity>,

  @Relation(
    parentColumn = "id",
    entityColumn = "categoryId"
  )
  val products: List<ProductRoomEntity> = emptyList()
) {
  // Filter consumed items in memory (Room @Relation doesn't support WHERE clauses well)
  fun filterConsumed(): CategoryWithItemsRoomEntity {
    return copy(items = items.filter { it.consumedDate == null })
  }

  // Get only consumed items
  fun getConsumedItems(): List<ItemRoomEntity> {
    return items.filter { it.consumedDate != null }
  }

  // Get only active items (not consumed)
  fun getActiveItems(): List<ItemRoomEntity> {
    return items.filter { it.consumedDate == null }
  }
}
