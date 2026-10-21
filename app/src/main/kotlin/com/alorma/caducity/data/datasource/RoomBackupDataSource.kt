package com.alorma.caducity.data.datasource

import androidx.room.withTransaction
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.backup.BackupCategory
import com.alorma.caducity.data.backup.BackupData
import com.alorma.caducity.data.backup.BackupProduct
import com.alorma.caducity.data.backup.BackupProductItem
import com.alorma.caducity.data.backup.BackupTransformation
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.dao.CategoryDao
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.dao.ProductDao
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.data.datasource.room.model.TransformationRoomEntity
import com.alorma.caducity.domain.backup.BackupDataSource
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
                canFreeze = product.canFreeze,
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
          usualDurationDays = category.usualDurationDays,
          canFreeze = category.canFreeze,
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
      transformations =
        database.transformationDao().getAllTransformationsSync().map { BackupTransformation(it.id, it.createdAt) },
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
      discardedDate = item.discardedDate,
      createdAt = item.createdAt,
      transformedDate = item.transformedDate,
      transformationId = item.transformationId,
      originTransformationId = item.originTransformationId,
    )

  override suspend fun importBackup(backup: BackupData) {
    database.withTransaction {
      // Clear existing data
      clearAllData()

      val transformationDao = database.transformationDao()
      backup.transformations.forEach { transformation ->
        transformationDao.insertTransformation(TransformationRoomEntity(transformation.id, transformation.createdAt))
      }

      backup.categories.forEach { category: BackupCategory ->
        val categoryEntity =
          CategoryRoomEntity(
            id = category.id,
            name = category.name,
            description = category.description,
            shape = shapeOrDefault(category.shape),
            icon = CategoryIcon(category.icon),
            sortOrder = category.sortOrder,
            usualDurationDays = category.usualDurationDays,
            canFreeze = category.canFreeze,
          )
        categoryDao.insertCategory(categoryEntity)

        category.products.forEach { product ->
          val productEntity =
            ProductRoomEntity(
              id = product.id,
              categoryId = product.categoryId,
              name = product.name,
              createdAt = product.createdAt,
              canFreeze = product.canFreeze,
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
              discardedDate = item.discardedDate,
              createdAt = item.createdAt,
              transformedDate = item.transformedDate,
              transformationId = item.transformationId,
              originTransformationId = item.originTransformationId,
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
                discardedDate = item.discardedDate,
                createdAt = item.createdAt,
                transformedDate = item.transformedDate,
                transformationId = item.transformationId,
                originTransformationId = item.originTransformationId,
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
    database.transformationDao().clearAllTransformations()
  }

  override fun validateBackup(backup: BackupData): Result<Unit> {
    return try {
      // Check version compatibility
      if (backup.version !in BackupData.MIN_SUPPORTED_VERSION..BackupData.CURRENT_VERSION) {
        return Result.failure(
          IllegalArgumentException(
            "Backup version ${backup.version} is not supported. Only versions ${BackupData.MIN_SUPPORTED_VERSION} to ${BackupData.CURRENT_VERSION} can be imported",
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
