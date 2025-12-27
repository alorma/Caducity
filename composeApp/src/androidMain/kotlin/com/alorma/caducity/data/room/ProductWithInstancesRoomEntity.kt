package com.alorma.caducity.data.room

import androidx.room.Embedded
import androidx.room.Relation

data class ProductWithInstancesRoomEntity(
  @Embedded
  val product: ProductRoomEntity,

  @Relation(
    parentColumn = "id",
    entityColumn = "productId"
  )
  val instances: List<ProductInstanceRoomEntity>
)
