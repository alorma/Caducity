package com.alorma.caducity.data.datasource

import androidx.room.withTransaction
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.backup.BackupData
import com.alorma.caducity.data.backup.BackupProduct
import com.alorma.caducity.data.backup.BackupProductItem
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.ItemRoomEntity
import com.alorma.caducity.domain.backup.BackupDataSource

class RoomBackupDataSource(
  private val database: AppDatabase,
  private val appClock: AppClock
) : BackupDataSource {

  private val categoryDao = database.categoryDao()
  private val itemDao = database.itemDao()

  override suspend fun exportBackup(): BackupData {
    val categories = categoryDao.getAllCategoriesSync()
    val items = itemDao.getAllItemsSync()

    // Group items by category ID
    val itemsByCategory = items.groupBy { it.categoryId }

    val backupProducts = categories.map { category ->
      BackupProduct(
        id = category.id,
        name = category.name,
        description = category.description,
        items = (itemsByCategory[category.id] ?: emptyList()).map { item ->
          BackupProductItem(
            id = item.id,
            identifier = item.identifier,
            expirationDate = item.expirationDate,
            pausedDate = item.pausedDate,
            remainingDays = item.remainingDays,
            consumedDate = item.consumedDate
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

      // Insert categories and items (mapping from old backup format)
      backup.products.forEach { backupProduct ->
        val category = CategoryRoomEntity(
          id = backupProduct.id,
          name = backupProduct.name,
          description = backupProduct.description
        )
        categoryDao.insertCategory(category)

        val items = backupProduct.items.map { backupInstance ->
          ItemRoomEntity(
            id = backupInstance.id,
            categoryId = backupProduct.id,
            identifier = backupInstance.identifier,
            productId = null, // Old backups don't have product variants
            expirationDate = backupInstance.expirationDate,
            pausedDate = backupInstance.pausedDate,
            remainingDays = backupInstance.remainingDays,
            consumedDate = backupInstance.consumedDate
          )
        }
        if (items.isNotEmpty()) {
          itemDao.insertItems(items)
        }
      }
    }
  }

  override suspend fun clearAllData() {
    // Delete items first due to foreign key constraint
    itemDao.clearAllItems()
    categoryDao.clearAllCategories()
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

        product.items.forEach { instance ->
          require(instance.id.isNotBlank()) { "Instance ID cannot be blank" }
          require(instance.expirationDate > 0) { "Instance expiration date must be positive" }
        }
      }

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
