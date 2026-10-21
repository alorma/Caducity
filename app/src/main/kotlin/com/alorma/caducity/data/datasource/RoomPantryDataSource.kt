package com.alorma.caducity.data.datasource

import androidx.room.withTransaction
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.dao.CategoryDao
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.dao.ProductDao
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.data.datasource.room.model.TransformationRoomEntity
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.AddedItems
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ItemSnapshot
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.ProductUsage
import com.alorma.caducity.domain.model.TransformIngredient
import com.alorma.caducity.domain.model.TransformResult
import com.alorma.caducity.domain.model.TransformedBatch
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.usecase.toExpirationInstant
import java.util.UUID
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPantryDataSource(
  private val database: AppDatabase,
  private val categoryDao: CategoryDao,
  private val itemDao: ItemDao,
  private val productDao: ProductDao,
  private val categoryMapper: CategoryRoomMapper,
  private val appClock: AppClock,
) : PantryDataSource {
  override suspend fun addItems(
    categoryId: String,
    productName: String,
    expiration: Instant,
    quantity: Int,
    frozenRemainingDays: Int?,
  ): AddedItems =
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
      AddedItems(productId = product.id, itemIds = ids, productCreated = created)
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

  override suspend fun consume(
    itemIds: List<String>,
    at: Instant,
  ) {
    database.withTransaction {
      itemIds.forEach { id ->
        itemDao.getItem(id)?.let {
          itemDao.updateItem(it.copy(consumedDate = at.toEpochMilliseconds(), pausedDate = null, remainingDays = null))
        }
      }
    }
  }

  override suspend fun freeze(
    itemIds: List<String>,
    at: Instant,
  ): List<String> =
    database.withTransaction {
      val today = at.date()
      itemIds.filter { id ->
        val item = itemDao.getItem(id) ?: return@filter false
        val days = Instant.fromEpochMilliseconds(item.expirationDate).date().toEpochDays() - today.toEpochDays()
        if (days < 0 || item.pausedDate != null || !canFreeze(item)) return@filter false
        itemDao.updateItem(item.copy(pausedDate = at.toEpochMilliseconds(), remainingDays = days.toInt()))
        true
      }
    }

  override suspend fun reschedule(
    itemIds: List<String>,
    newExpiration: Instant,
  ) {
    database.withTransaction {
      itemIds.forEach { id ->
        itemDao.getItem(id)?.let {
          itemDao.updateItem(
            it.copy(expirationDate = newExpiration.toEpochMilliseconds(), pausedDate = null, remainingDays = null),
          )
        }
      }
    }
  }

  override suspend fun transform(
    ingredients: List<TransformIngredient>,
    results: List<TransformResult>,
    at: Instant,
  ): TransformedBatch =
    database.withTransaction {
      val now = at.toEpochMilliseconds()
      val today = at.date()
      val transformationId =
        if (ingredients.isEmpty()) {
          null
        } else {
          UUID.randomUUID().toString().also {
            database.transformationDao().insertTransformation(TransformationRoomEntity(it, now))
          }
        }

      val ingredientIds = ingredients.flatMap { it.itemIds }.distinct()
      val ingredientSnapshots = snapshots(ingredientIds)

      val resultItemIds = mutableListOf<String>()
      val createdProductIds = mutableListOf<String>()
      results.forEach { result ->
        val (product, created) = findOrCreateProduct(result.categoryId, result.name)
        if (created) createdProductIds += product.id
        // Frozen results keep the days until their date, like the add-frozen path.
        val remainingDays = (result.expiration.toEpochDays() - today.toEpochDays()).toInt()
        repeat(result.servings) { index ->
          val id = UUID.randomUUID().toString()
          val frozen = index < result.frozenCount
          itemDao.insertItem(
            ItemRoomEntity(
              id = id,
              categoryId = result.categoryId,
              identifier = "",
              productId = product.id,
              expirationDate = result.expiration.toExpirationInstant().toEpochMilliseconds(),
              pausedDate = now.takeIf { frozen },
              remainingDays = remainingDays.takeIf { frozen },
              createdAt = now,
              originTransformationId = transformationId,
            ),
          )
          resultItemIds += id
        }
      }

      ingredientIds.forEach { id ->
        itemDao.getItem(id)?.let {
          itemDao.updateItem(
            it.copy(
              transformedDate = now,
              transformationId = transformationId,
              pausedDate = null,
              remainingDays = null,
            ),
          )
        }
      }
      TransformedBatch(transformationId, resultItemIds, createdProductIds, ingredientSnapshots)
    }

  override suspend fun undoTransform(batch: TransformedBatch) {
    database.withTransaction {
      itemDao.deleteItems(batch.resultItemIds)
      batch.createdProductIds.forEach { productDao.deleteProductIfUnused(it) }
      restoreSnapshots(batch.ingredientSnapshots)
      batch.transformationId?.let { database.transformationDao().deleteTransformation(it) }
    }
  }

  override suspend fun snapshots(itemIds: List<String>): List<ItemSnapshot> =
    itemIds.mapNotNull { id ->
      itemDao.getItem(id)?.let {
        ItemSnapshot(
          itemId = it.id,
          expirationMillis = it.expirationDate,
          pausedMillis = it.pausedDate,
          remainingDays = it.remainingDays,
          consumedMillis = it.consumedDate,
          discardedMillis = it.discardedDate,
          transformedMillis = it.transformedDate,
          transformationId = it.transformationId,
        )
      }
    }

  override suspend fun restoreSnapshots(snapshots: List<ItemSnapshot>) {
    database.withTransaction {
      snapshots.forEach { snap ->
        itemDao.getItem(snap.itemId)?.let {
          itemDao.updateItem(
            it.copy(
              expirationDate = snap.expirationMillis,
              pausedDate = snap.pausedMillis,
              remainingDays = snap.remainingDays,
              consumedDate = snap.consumedMillis,
              discardedDate = snap.discardedMillis,
              transformedDate = snap.transformedMillis,
              transformationId = snap.transformationId,
            ),
          )
        }
      }
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

  override suspend fun unfreezeAll(
    itemIds: List<String>,
    newExpiration: Instant,
  ): List<String> =
    database.withTransaction {
      itemIds.filter { id ->
        val item = itemDao.getItem(id) ?: return@filter false
        if (item.pausedDate == null) return@filter false
        itemDao.updateItem(
          item.copy(expirationDate = newExpiration.toEpochMilliseconds(), pausedDate = null, remainingDays = null),
        )
        true
      }
    }

  override fun observeActiveItems(): Flow<List<ActiveItem>> =
    itemDao.observeActiveItemRows().map { rows ->
      rows.map { row ->
        ActiveItem(
          itemId = row.itemId,
          productId = row.productId,
          name = row.name,
          categoryId = row.categoryId,
          expiration = Instant.fromEpochMilliseconds(row.expirationDate).date(),
          frozen = row.pausedDate != null,
          frozenSince = row.pausedDate?.let { Instant.fromEpochMilliseconds(it) },
          remainingDays = row.remainingDays,
          createdAt = row.createdAt?.let { Instant.fromEpochMilliseconds(it) },
          canFreeze = row.canFreeze,
        )
      }
    }

  override fun observeCategories(): Flow<List<Category>> =
    categoryDao.getCategoriesOrdered().map { it.map(categoryMapper::toModel) }

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

  override suspend fun setProductCanFreeze(
    productId: String,
    canFreeze: Boolean?,
  ) {
    database.withTransaction {
      val product = productDao.getProduct(productId) ?: return@withTransaction
      val inherited = categoryDao.getCategory(product.categoryId)?.canFreeze ?: true
      productDao.updateProduct(product.copy(canFreeze = canFreeze?.takeIf { it != inherited }))
    }
  }

  /** Effective value: the product's override, else the category's; a unit without product follows its category. */
  private suspend fun canFreeze(item: ItemRoomEntity): Boolean =
    item.productId?.let { productDao.getProduct(it)?.canFreeze }
      ?: categoryDao.getCategory(item.categoryId)?.canFreeze
      ?: true

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
