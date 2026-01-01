package com.alorma.caducity.data.datasource.room

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
  val pausedDate: Long? = null, // When frozen - null means not frozen
  val remainingDays: Int? = null, // Days remaining when frozen
  val consumedDate: Long? = null, // When consumed - null means not consumed
)
