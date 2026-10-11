package com.alorma.caducity.data.datasource

import androidx.room.withTransaction
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.backup.BackupCategory
import com.alorma.caducity.data.backup.BackupData
import com.alorma.caducity.data.backup.BackupProduct
import com.alorma.caducity.data.backup.BackupProductItem
import com.alorma.caducity.data.backup.upgradeToV2
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.dao.CategoryDao
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.dao.ProductDao
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.domain.backup.BackupDataSource
import com.alorma.caducity.domain.model.CategoryDefaults
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape

class RoomBackupDataSource(
  private val database: AppDatabase,
  private val categoryDao: CategoryDao,
  private val itemDao: ItemDao,
  private val productDao: ProductDao,
  private val appClock: AppClock,
) : BackupDataSource {
  override suspend fun exportBackup(): BackupData {
    val categories = categoryDao.getCategoriesOrderedSync()
    val products = productDao.getAllProductsSync()
    val items = itemDao.getAllItemsSync()

    val backupCategories =
      categories.map { category ->

        val productsByCategory =
          products
            .filter { product -> product.categoryId == category.id }
            .map { product ->
              BackupProduct(
                id = product.id,
                categoryId = product.categoryId,
                name = product.name,
                createdAt = product.createdAt,
                items =
                  items
                    .filter { item -> item.categoryId == category.id }
                    .filter { item -> item.productId == product.id }
                    .map(::mapEntityToBackup),
              )
            }

        BackupCategory(
          id = category.id,
          name = category.name,
          description = category.description,
          shape = category.shape.name,
          icon = category.icon.id,
          sortOrder = category.sortOrder,
          soonThresholdDays = category.soonThresholdDays,
          products = productsByCategory,
          standaloneItems =
            items
              .filter { item -> item.categoryId == category.id }
              .filter { item -> item.productId == null }
              .map(::mapEntityToBackup),
        )
      }

    return BackupData(
      version = BackupData.CURRENT_VERSION,
      exportDate = appClock.now().toEpochMilliseconds(),
      categories = backupCategories,
    )
  }

  private fun mapEntityToBackup(item: ItemRoomEntity): BackupProductItem =
    BackupProductItem(
      id = item.id,
      identifier = item.identifier,
      expirationDate = item.expirationDate,
      pausedDate = item.pausedDate,
      remainingDays = item.remainingDays,
      consumedDate = item.consumedDate,
      packSize = item.packSize,
      discardedDate = item.discardedDate,
      createdAt = item.createdAt,
    )

  override suspend fun importBackup(backup: BackupData) {
    val data = backup.upgradeToV2()
    database.withTransaction {
      // Clear existing data
      clearAllData()

      // Insert categories and items (mapping from old backup format)
      data.categories.forEach { category: BackupCategory ->
        val defaults = CategoryDefaults.forName(category.name, category.sortOrder ?: 0)
        val categoryEntity =
          CategoryRoomEntity(
            id = category.id,
            name = category.name,
            description = category.description,
            shape = category.shape?.let(::shapeOrDefault) ?: defaults.shape,
            icon = category.icon?.let { CategoryIcon(it) } ?: defaults.icon,
            sortOrder = category.sortOrder ?: 0,
            soonThresholdDays = category.soonThresholdDays,
          )
        categoryDao.insertCategory(categoryEntity)

        category.products.forEach { product ->
          val productEntity =
            ProductRoomEntity(
              id = product.id,
              categoryId = product.categoryId,
              name = product.name,
              createdAt = product.createdAt,
            )
          productDao.insertProduct(productEntity)
        }

        category.standaloneItems.forEach { item ->
          val itemEntity =
            ItemRoomEntity(
              id = item.id,
              categoryId = categoryEntity.id,
              identifier = item.identifier,
              productId = null,
              expirationDate = item.expirationDate,
              pausedDate = item.pausedDate,
              remainingDays = item.remainingDays,
              consumedDate = item.consumedDate,
              packSize = item.packSize,
              discardedDate = item.discardedDate,
              createdAt = item.createdAt,
            )
          itemDao.insertItem(itemEntity)
        }

        category.products.forEach { product ->
          product.items.forEach { item ->
            val itemEntity =
              ItemRoomEntity(
                id = item.id,
                categoryId = categoryEntity.id,
                identifier = item.identifier,
                productId = product.id,
                expirationDate = item.expirationDate,
                pausedDate = item.pausedDate,
                remainingDays = item.remainingDays,
                consumedDate = item.consumedDate,
                packSize = item.packSize,
                discardedDate = item.discardedDate,
                createdAt = item.createdAt,
              )
            itemDao.insertItem(itemEntity)
          }
        }
      }
    }
  }

  private fun shapeOrDefault(name: String): CategoryShape =
    CategoryShape.entries.firstOrNull { it.name == name } ?: CategoryShape.COOKIE

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
          IllegalArgumentException(
            "Backup version ${backup.version} is not supported. Current version: ${BackupData.CURRENT_VERSION}",
          ),
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
