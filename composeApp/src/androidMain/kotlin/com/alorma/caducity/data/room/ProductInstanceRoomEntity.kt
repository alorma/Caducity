package com.alorma.caducity.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "product_instances",
  foreignKeys = [
    ForeignKey(
      entity = ProductRoomEntity::class,
      parentColumns = ["id"],
      childColumns = ["productId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("productId")]
)
data class ProductInstanceRoomEntity(
  @PrimaryKey
  val id: String,
  val productId: String,
  val identifier: String,
  val expirationDate: Long,
)
