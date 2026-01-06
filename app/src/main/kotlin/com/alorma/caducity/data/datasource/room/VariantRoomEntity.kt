package com.alorma.caducity.data.datasource.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "variants",
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
data class VariantRoomEntity(
  @PrimaryKey
  val id: String,
  val productId: String,
  val name: String,
  val createdAt: Long,
)
