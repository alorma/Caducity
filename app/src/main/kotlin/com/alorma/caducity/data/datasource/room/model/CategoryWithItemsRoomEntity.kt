package com.alorma.caducity.data.datasource.room.model

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithItemsRoomEntity(
  @Embedded
  val category: CategoryRoomEntity,

  @Relation(
    parentColumn = "id",
    entityColumn = "categoryId"
  )
  val products: List<ProductRoomEntity> = emptyList(),

  @Relation(
    parentColumn = "id",
    entityColumn = "categoryId"
  )
  val items: List<ItemRoomEntity>,
)
