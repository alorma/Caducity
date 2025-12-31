package com.alorma.caducity.data.datasource.room

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
) {
  // Filter consumed instances in memory (Room @Relation doesn't support WHERE clauses well)
  fun filterConsumed(): ProductWithInstancesRoomEntity {
    return copy(instances = instances.filter { it.consumedDate == null })
  }
}
