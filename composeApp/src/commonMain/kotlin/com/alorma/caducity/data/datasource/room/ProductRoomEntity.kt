package com.alorma.caducity.data.datasource.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductRoomEntity(
  @PrimaryKey
  val id: String,
  val name: String,
  val description: String,
)
