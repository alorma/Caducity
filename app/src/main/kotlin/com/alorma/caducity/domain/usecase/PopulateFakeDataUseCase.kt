package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.backup.BackupDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryDefaults
import com.alorma.caducity.domain.model.NewItem
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.usecase.fakedata.FakeDataStrategy
import java.util.UUID
import kotlin.time.Instant
import kotlinx.collections.immutable.persistentListOf

/**
 * Use case to populate the database with fake data using a strategy pattern
 *
 * Different strategies can create different data sets for different purposes:
 * - Test data with comprehensive status coverage
 * - PlayStore data with consistent products for screenshots
 * - Custom data sets as needed
 */
class PopulateFakeDataUseCase(
  private val backupDataSource: BackupDataSource,
  private val categoryDataSource: CategoryDataSource,
  private val itemDataSource: ItemDataSource,
  private val productDataSource: ProductDataSource,
  private val appClock: AppClock,
) {
  suspend fun execute(strategy: FakeDataStrategy): Result<Unit> =
    try {
      // Clear all existing data first
      backupDataSource.clearAllData()

      val now = appClock.now()

      // Get category configurations from the strategy
      val categoryConfigs = strategy.getCategoryConfigs(now)

      // Create each category with its products and items, in order
      categoryConfigs.forEachIndexed { index, categoryConfig ->
        val defaults = CategoryDefaults.forName(categoryConfig.name, index)
        val category =
          Category(
            id = UUID.randomUUID().toString(),
            name = categoryConfig.name,
            description = categoryConfig.description,
            shape = categoryConfig.shape ?: defaults.shape,
            icon = categoryConfig.icon ?: defaults.icon,
            sortOrder = index,
            soonThresholdDays = categoryConfig.soonThresholdDays,
          )

        // Create category first (empty)
        categoryDataSource.createCategory(category, persistentListOf())

        // Every item belongs to a product; the free-text `identifier` is no longer used by the app.
        val productIdsByName = mutableMapOf<String, String>()

        suspend fun productIdFor(name: String): String =
          productIdsByName.getOrPut(name.normalizeForSearch()) { productDataSource.createProduct(category.id, name).id }

        suspend fun addItem(
          productName: String,
          expirationDate: Instant,
          shouldFreeze: Boolean,
          remainingDaysWhenFrozen: Int,
          shouldConsume: Boolean,
          packSize: Int? = null,
        ) {
          val newItem =
            NewItem(
              identifier = "",
              productId = productIdFor(productName),
              expirationDate = expirationDate,
              packSize = packSize,
            )
          val itemId = itemDataSource.addItem(category.id, newItem)
          when {
            shouldFreeze -> itemDataSource.freezeItem(itemId, remainingDays = remainingDaysWhenFrozen)
            shouldConsume -> itemDataSource.markItemAsConsumed(itemId)
          }
        }

        categoryConfig.products.forEach { productConfig ->
          productIdFor(productConfig.name)
          productConfig.items.forEach { item ->
            addItem(
              productConfig.name,
              item.expirationDate,
              item.shouldFreeze,
              item.remainingDaysWhenFrozen,
              item.shouldConsume,
              item.packSize,
            )
          }
        }

        // Legacy standalone configs become products named after their identifier.
        categoryConfig.standaloneItems.forEach { item ->
          addItem(
            item.identifier,
            item.expirationDate,
            item.shouldFreeze,
            item.remainingDaysWhenFrozen,
            item.shouldConsume,
          )
        }
      }

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
}
