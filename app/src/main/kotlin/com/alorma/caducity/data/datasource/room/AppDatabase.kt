package com.alorma.caducity.data.datasource.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alorma.caducity.data.datasource.room.dao.CategoryDao
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.dao.ProductDao
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity

@Database(
  entities = [
    CategoryRoomEntity::class,
    ItemRoomEntity::class,
    ProductRoomEntity::class,
  ],
  version = 2,
  exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun categoryDao(): CategoryDao

  abstract fun itemDao(): ItemDao

  abstract fun productDao(): ProductDao
}
