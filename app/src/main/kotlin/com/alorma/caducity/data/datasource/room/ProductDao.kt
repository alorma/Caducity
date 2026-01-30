package com.alorma.caducity.data.datasource.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

  @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
  fun getProductsByCategory(categoryId: String): Flow<List<ProductRoomEntity>>

  @Query("SELECT * FROM products WHERE id = :productId")
  suspend fun getProduct(productId: String): ProductRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductRoomEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<ProductRoomEntity>)

  @Query("DELETE FROM products WHERE id = :productId")
  suspend fun deleteProduct(productId: String)

  @Query("SELECT COUNT(*) FROM items WHERE productId = :productId AND consumedDate IS NULL")
  suspend fun getActiveItemCount(productId: String): Int

  @Query("SELECT * FROM products")
  suspend fun getAllProductsSync(): List<ProductRoomEntity>

  @Query("DELETE FROM products")
  suspend fun clearAllProducts()
}
