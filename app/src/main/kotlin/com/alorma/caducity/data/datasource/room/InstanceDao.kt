package com.alorma.caducity.data.datasource.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InstanceDao {

  @Query("SELECT * FROM product_instances WHERE productId = :productId")
  fun getProductInstances(productId: String): Flow<List<ProductInstanceRoomEntity>>

  @Query("SELECT * FROM product_instances WHERE id = :instanceId")
  suspend fun getProductInstance(instanceId: String): ProductInstanceRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProductInstance(instance: ProductInstanceRoomEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProductInstances(instances: List<ProductInstanceRoomEntity>)

  @Update
  suspend fun updateProductInstance(instance: ProductInstanceRoomEntity)

  @Query("DELETE FROM product_instances WHERE id = :instanceId")
  suspend fun deleteProductInstance(instanceId: String)

  // Backup & Restore methods
  @Query("SELECT * FROM product_instances")
  suspend fun getAllProductInstancesSync(): List<ProductInstanceRoomEntity>

  @Query("DELETE FROM product_instances")
  suspend fun clearAllProductInstances()
}
