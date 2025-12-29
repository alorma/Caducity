package com.alorma.caducity.data.datasource.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

  @Transaction
  @Query("SELECT * FROM products")
  fun getAllProductsWithInstances(): Flow<List<ProductWithInstancesRoomEntity>>

  @Transaction
  @Query("SELECT * FROM products WHERE id = :productId")
  fun getProductWithInstances(productId: String): Flow<ProductWithInstancesRoomEntity?>

  @Query("SELECT * FROM products")
  fun getAllProducts(): Flow<List<ProductRoomEntity>>

  @Query("SELECT * FROM products WHERE id = :productId")
  suspend fun getProduct(productId: String): ProductRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductRoomEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<ProductRoomEntity>)

  @Query("DELETE FROM products WHERE id = :productId")
  suspend fun deleteProduct(productId: String)

  @Query("SELECT * FROM product_instances WHERE productId = :productId")
  fun getProductInstances(productId: String): Flow<List<ProductInstanceRoomEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProductInstance(instance: ProductInstanceRoomEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProductInstances(instances: List<ProductInstanceRoomEntity>)

  @Query("DELETE FROM product_instances WHERE id = :instanceId")
  suspend fun deleteProductInstance(instanceId: String)
}
