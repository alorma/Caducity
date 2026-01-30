package com.alorma.caducity.data.datasource.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryRoomEntity(
  @PrimaryKey
  val id: String,
  val name: String,
  val description: String,
)
