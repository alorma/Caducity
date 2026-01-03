package com.alorma.caducity.data.datasource

import androidx.room.withTransaction
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.backup.BackupData
import com.alorma.caducity.data.backup.BackupProduct
import com.alorma.caducity.data.backup.BackupProductInstance
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.ProductInstanceRoomEntity
import com.alorma.caducity.data.datasource.room.ProductRoomEntity
import com.alorma.caducity.domain.backup.BackupDataSource

class RoomBackupDataSource(
  private val database: AppDatabase,
  private val appClock: AppClock
) : BackupDataSource {

  private val productDao = database.productDao()

  override suspend fun exportBackup(): BackupData {
    val products = productDao.getAllProductsSync()
    val instances = productDao.getAllProductInstancesSync()

    // Group instances by product ID
    val instancesByProduct = instances.groupBy { it.productId }

    val backupProducts = products.map { product ->
      BackupProduct(
        id = product.id,
        name = product.name,
        description = product.description,
        instances = (instancesByProduct[product.id] ?: emptyList()).map { instance ->
          BackupProductInstance(
            id = instance.id,
            identifier = instance.identifier,
            expirationDate = instance.expirationDate,
            pausedDate = instance.pausedDate,
            remainingDays = instance.remainingDays,
            consumedDate = instance.consumedDate
          )
        }
      )
    }

    return BackupData(
      version = BackupData.CURRENT_VERSION,
      exportDate = appClock.now().toEpochMilliseconds(),
      products = backupProducts
    )
  }

  override suspend fun importBackup(backup: BackupData) {
    database.withTransaction {
      // Clear existing data
      clearAllData()

      // Insert products and instances
      backup.products.forEach { backupProduct ->
        val product = ProductRoomEntity(
          id = backupProduct.id,
          name = backupProduct.name,
          description = backupProduct.description
        )
        productDao.insertProduct(product)

        val instances = backupProduct.instances.map { backupInstance ->
          ProductInstanceRoomEntity(
            id = backupInstance.id,
            productId = backupProduct.id,
            identifier = backupInstance.identifier,
            expirationDate = backupInstance.expirationDate,
            pausedDate = backupInstance.pausedDate,
            remainingDays = backupInstance.remainingDays,
            consumedDate = backupInstance.consumedDate
          )
        }
        if (instances.isNotEmpty()) {
          productDao.insertProductInstances(instances)
        }
      }
    }
  }

  override suspend fun clearAllData() {
    // Delete instances first due to foreign key constraint
    productDao.clearAllProductInstances()
    productDao.clearAllProducts()
  }

  override fun validateBackup(backup: BackupData): Result<Unit> {
    return try {
      // Check version compatibility
      if (backup.version > BackupData.CURRENT_VERSION) {
        return Result.failure(
          IllegalArgumentException("Backup version ${backup.version} is not supported. Current version: ${BackupData.CURRENT_VERSION}")
        )
      }

      // Validate data structure
      backup.products.forEach { product ->
        require(product.id.isNotBlank()) { "Product ID cannot be blank" }
        require(product.name.isNotBlank()) { "Product name cannot be blank" }

        product.instances.forEach { instance ->
          require(instance.id.isNotBlank()) { "Instance ID cannot be blank" }
          require(instance.identifier.isNotBlank()) { "Instance identifier cannot be blank" }
          require(instance.expirationDate > 0) { "Instance expiration date must be positive" }
        }
      }

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
