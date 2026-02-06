package com.alorma.caducity.data.datasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

  @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
  fun getProductsByCategory(categoryId: String): Flow<List<ProductRoomEntity>>

  @Query("SELECT * FROM products WHERE id = :productId")
  suspend fun getProduct(productId: String): ProductRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductRoomEntity)

  @Query("SELECT * FROM products")
  suspend fun getAllProductsSync(): List<ProductRoomEntity>
}
