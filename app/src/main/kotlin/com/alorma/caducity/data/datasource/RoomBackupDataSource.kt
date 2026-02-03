package com.alorma.caducity.data.datasource

import androidx.room.withTransaction
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.backup.BackupCategory
import com.alorma.caducity.data.backup.BackupData
import com.alorma.caducity.data.backup.BackupProduct
import com.alorma.caducity.data.backup.BackupProductItem
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.ItemRoomEntity
import com.alorma.caducity.domain.backup.BackupDataSource

class RoomBackupDataSource(
  private val database: AppDatabase,
  private val appClock: AppClock
) : BackupDataSource {

  private val categoryDao = database.categoryDao()
  private val itemDao = database.itemDao()
  private val productDao = database.productDao()

  override suspend fun exportBackup(): BackupData {
    val categories = categoryDao.getAllCategoriesSync()
    val products = productDao.getAllProductsSync()
    val items = itemDao.getAllItemsSync()

    val backupCategories = categories.map { category ->

      val productsByCategory = products
        .filter { product -> product.categoryId == category.id }
        .map { product ->
          BackupProduct(
            id = product.id,
            categoryId = product.categoryId,
            name = product.name,
            createdAt = product.createdAt,
            items = items
              .filter { item -> item.categoryId == category.id }
              .filter { item -> item.productId == product.id }
              .map(::mapEntityToBackup),
          )
        }

      BackupCategory(
        id = category.id,
        name = category.name,
        description = category.description,
        products = productsByCategory,
        standaloneItems = items.filter { item -> item.categoryId == category.id }
          .filter { item -> item.productId == null }
          .map(::mapEntityToBackup)
      )
    }

    return BackupData(
      version = BackupData.CURRENT_VERSION,
      exportDate = appClock.now().toEpochMilliseconds(),
      categories = backupCategories
    )
  }

  private fun mapEntityToBackup(item: ItemRoomEntity): BackupProductItem = BackupProductItem(
    id = item.id,
    identifier = item.identifier,
    expirationDate = item.expirationDate,
    pausedDate = item.pausedDate,
    remainingDays = item.remainingDays,
    consumedDate = item.consumedDate
  )


  override suspend fun importBackup(backup: BackupData) {
    database.withTransaction {
      /*
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
       */
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
      backup.categories.forEach { category ->
        require(category.id.isNotBlank()) { "Category ID cannot be blank" }
        require(category.name.isNotBlank()) { "Category name cannot be blank" }

        category.products.forEach { product ->
          require(product.id.isNotBlank()) { "Product ID cannot be blank" }
          require(product.name.isNotBlank()) { "Product name cannot be blank" }
        }

        buildList {
          addAll(category.standaloneItems)
          addAll(category.products.flatMap { product -> product.items })
        }.forEach { item ->
          require(item.id.isNotBlank()) { "Instance ID cannot be blank" }
          require(item.expirationDate > 0) { "Instance expiration date must be positive" }
        }

        category.standaloneItems.forEach { item ->
          require(item.id.isNotBlank()) { "Instance ID cannot be blank" }
          require(item.expirationDate > 0) { "Instance expiration date must be positive" }
        }
      }

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
