package com.alorma.caducity.data.datasource.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transformations")
data class TransformationRoomEntity(
  @PrimaryKey
  val id: String,
  val createdAt: Long,
)
