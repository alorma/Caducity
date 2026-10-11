package com.alorma.caducity.data.datasource.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryRoomEntity(
  @PrimaryKey
  val id: String,
  val name: String,
  val description: String,
  @ColumnInfo(defaultValue = "COOKIE")
  val shape: String = "COOKIE",
  @ColumnInfo(defaultValue = "eco")
  val icon: String = "eco",
  @ColumnInfo(defaultValue = "0")
  val sortOrder: Int = 0,
)
