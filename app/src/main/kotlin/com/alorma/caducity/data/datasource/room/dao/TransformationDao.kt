package com.alorma.caducity.data.datasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alorma.caducity.data.datasource.room.model.TransformationRoomEntity

@Dao
interface TransformationDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransformation(transformation: TransformationRoomEntity)

  @Query("SELECT * FROM transformations")
  suspend fun getAllTransformationsSync(): List<TransformationRoomEntity>

  @Query("DELETE FROM transformations WHERE id = :id")
  suspend fun deleteTransformation(id: String)

  @Query("DELETE FROM transformations")
  suspend fun clearAllTransformations()
}
