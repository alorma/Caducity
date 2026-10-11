package com.alorma.caducity.data.datasource

import androidx.room.withTransaction
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.dao.CategoryDao
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.dao.ProductDao
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.AddedBatch
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.ProductUsage
import com.alorma.caducity.domain.model.normalizeForSearch
import java.util.UUID
import kotlin.time.Instant

class RoomPantryDataSource(
  private val database: AppDatabase,
  private val categoryDao: CategoryDao,
  private val itemDao: ItemDao,
  private val productDao: ProductDao,
  private val categoryMapper: CategoryRoomMapper,
  private val appClock: AppClock,
) : PantryDataSource {
  override suspend fun addBatch(
    categoryId: String,
    productName: String,
    expiration: Instant,
    quantity: Int,
    frozenRemainingDays: Int?,
  ): AddedBatch =
    database.withTransaction {
      val (product, created) = findOrCreateProduct(categoryId, productName)
      val now = appClock.now().toEpochMilliseconds()
      val ids = List(quantity) { UUID.randomUUID().toString() }
      ids.forEach { id ->
        itemDao.insertItem(
          ItemRoomEntity(
            id = id,
            categoryId = categoryId,
            identifier = "",
            productId = product.id,
            expirationDate = expiration.toEpochMilliseconds(),
            pausedDate = now.takeIf { frozenRemainingDays != null },
            remainingDays = frozenRemainingDays,
            createdAt = now,
          ),
        )
      }
      AddedBatch(productId = product.id, itemIds = ids, productCreated = created)
    }

  override suspend fun undoAdd(batch: AddedBatch) {
    database.withTransaction {
      itemDao.deleteItems(batch.itemIds)
      if (batch.productCreated) productDao.deleteProductIfUnused(batch.productId)
    }
  }

  override suspend fun updateItem(
    itemId: String,
    categoryId: String,
    productName: String,
    expiration: Instant,
    frozenRemainingDays: Int?,
  ) {
    database.withTransaction {
      val item = itemDao.getItem(itemId) ?: throw PantryError.ItemNotFound
      val (product, _) = findOrCreateProduct(categoryId, productName)
      val frozen = item.pausedDate != null
      itemDao.updateItem(
        item.copy(
          categoryId = categoryId,
          productId = product.id,
          expirationDate = expiration.toEpochMilliseconds(),
          remainingDays = if (frozen) frozenRemainingDays ?: item.remainingDays else item.remainingDays,
        ),
      )
    }
  }

  override suspend fun moveItem(
    itemId: String,
    categoryId: String,
  ) {
    database.withTransaction {
      val item = itemDao.getItem(itemId) ?: throw PantryError.ItemNotFound
      val name = item.productId?.let { productDao.getProduct(it)?.name } ?: item.identifier
      if (name.isBlank()) {
        itemDao.moveItem(itemId, categoryId, productId = null)
      } else {
        val (product, _) = findOrCreateProduct(categoryId, name)
        itemDao.moveItem(itemId, categoryId, product.id)
      }
    }
  }

  override suspend fun discard(
    itemIds: List<String>,
    at: Instant,
  ) {
    database.withTransaction {
      itemIds.forEach { itemDao.discardItem(it, at.toEpochMilliseconds()) }
    }
  }

  override suspend fun restore(itemIds: List<String>) {
    database.withTransaction {
      itemIds.forEach { itemDao.restoreItem(it) }
    }
  }

  override suspend fun unfreeze(
    itemId: String,
    newExpiration: Instant,
  ) {
    database.withTransaction {
      val item = itemDao.getItem(itemId) ?: throw PantryError.ItemNotFound
      itemDao.updateItem(
        item.copy(
          expirationDate = newExpiration.toEpochMilliseconds(),
          pausedDate = null,
          remainingDays = null,
        ),
      )
    }
  }

  override suspend fun getProductUsage(): List<ProductUsage> =
    productDao.getProductUsage().map {
      ProductUsage(
        productId = it.productId,
        name = it.name,
        categoryId = it.categoryId,
        uses = it.uses,
        lastUsed = Instant.fromEpochMilliseconds(it.lastUsed),
      )
    }

  override suspend fun getCategories(): List<Category> =
    categoryDao.getCategoriesOrderedSync().map(categoryMapper::toModel)

  override suspend fun getNextSortOrder(): Int = categoryDao.getMaxSortOrder() + 1

  override suspend fun updateCategory(category: Category) {
    categoryDao.updateCategory(categoryMapper.toEntity(category))
  }

  override suspend fun createCategories(categories: List<Category>) {
    categoryDao.insertCategories(categories.map(categoryMapper::toEntity))
  }

  private suspend fun findOrCreateProduct(
    categoryId: String,
    name: String,
  ): Pair<ProductRoomEntity, Boolean> {
    val normalized = name.normalizeForSearch()
    val existing =
      productDao.getProductsByCategorySync(categoryId).firstOrNull { it.name.normalizeForSearch() == normalized }
    if (existing != null) return existing to false

    val product =
      ProductRoomEntity(
        id = UUID.randomUUID().toString(),
        categoryId = categoryId,
        name = name.trim().replace(Regex("\\s+"), " "),
        createdAt = appClock.now().toEpochMilliseconds(),
      )
    productDao.insertProduct(product)
    return product to true
  }
}
