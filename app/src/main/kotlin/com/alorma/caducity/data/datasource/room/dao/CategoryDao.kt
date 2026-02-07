package com.alorma.caducity.data.datasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.CategoryWithItemsRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

  @Transaction
  @Query("SELECT * FROM categories")
  fun getAllCategoriesWithItems(): Flow<List<CategoryWithItemsRoomEntity>>

  @Transaction
  @Query("SELECT * FROM categories WHERE id = :categoryId")
  fun getCategoryWithItems(categoryId: String): Flow<CategoryWithItemsRoomEntity?>

  @Query("SELECT * FROM categories")
  fun getAllCategories(): Flow<List<CategoryRoomEntity>>

  @Query("SELECT * FROM categories WHERE id = :categoryId")
  suspend fun getCategory(categoryId: String): CategoryRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CategoryRoomEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<CategoryRoomEntity>)

  @Query("DELETE FROM categories WHERE id = :categoryId")
  suspend fun deleteCategory(categoryId: String)

  // Backup & Restore methods
  @Query("SELECT * FROM categories")
  suspend fun getAllCategoriesSync(): List<CategoryRoomEntity>

  @Query("DELETE FROM categories")
  suspend fun clearAllCategories()
}
