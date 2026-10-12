package com.alorma.caducity.data.datasource.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alorma.caducity.data.datasource.room.dao.CategoryDao
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.dao.ProductDao
import com.alorma.caducity.data.datasource.room.dao.TransformationDao
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.data.datasource.room.model.TransformationRoomEntity

@Database(
  entities = [
    CategoryRoomEntity::class,
    ItemRoomEntity::class,
    ProductRoomEntity::class,
    TransformationRoomEntity::class,
  ],
  version = 4,
  exportSchema = true,
)
@TypeConverters(AvatarConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun categoryDao(): CategoryDao

  abstract fun itemDao(): ItemDao

  abstract fun productDao(): ProductDao

  abstract fun transformationDao(): TransformationDao
}
