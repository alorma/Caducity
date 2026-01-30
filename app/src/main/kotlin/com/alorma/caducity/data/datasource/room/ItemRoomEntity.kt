package com.alorma.caducity.data.datasource.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "items",
  foreignKeys = [
    ForeignKey(
      entity = CategoryRoomEntity::class,
      parentColumns = ["id"],
      childColumns = ["categoryId"],
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity = ProductRoomEntity::class,
      parentColumns = ["id"],
      childColumns = ["productId"],
      onDelete = ForeignKey.SET_NULL
    )
  ],
  indices = [Index("categoryId"), Index("productId")]
)
data class ItemRoomEntity(
  @PrimaryKey
  val id: String,
  val categoryId: String,
  val identifier: String,
  val productId: String? = null,
  val expirationDate: Long,
  val pausedDate: Long? = null, // When frozen - null means not frozen
  val remainingDays: Int? = null, // Days remaining when frozen
  val consumedDate: Long? = null, // When consumed - null means not consumed
)
