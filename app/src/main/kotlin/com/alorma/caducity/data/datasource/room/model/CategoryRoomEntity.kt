package com.alorma.caducity.data.datasource.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape

@Entity(tableName = "categories")
data class CategoryRoomEntity(
  @PrimaryKey
  val id: String,
  val name: String,
  val description: String,
  @ColumnInfo(defaultValue = "COOKIE")
  val shape: CategoryShape = CategoryShape.COOKIE,
  @ColumnInfo(defaultValue = "eco")
  val icon: CategoryIcon = CategoryIcon.DEFAULT,
  @ColumnInfo(defaultValue = "0")
  val sortOrder: Int = 0,
  val usualDurationDays: Int? = null,
  @ColumnInfo(defaultValue = "1")
  val canFreeze: Boolean = true,
)
