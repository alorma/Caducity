package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.backup.BackupDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.NewItem
import com.alorma.caducity.domain.usecase.fakedata.FakeDataStrategy
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

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

  suspend fun execute(strategy: FakeDataStrategy): Result<Unit> {
    return try {
      // Clear all existing data first
      backupDataSource.clearAllData()

      val now = appClock.now()

      // Get category configurations from the strategy
      val categoryConfigs = strategy.getCategoryConfigs(now)

      // Create each category with its products and items
      categoryConfigs.forEach { categoryConfig ->
        val category = Category(
          id = UUID.randomUUID().toString(),
          name = categoryConfig.name,
          description = categoryConfig.description
        )

        // Create category first (empty)
        categoryDataSource.createCategory(category, persistentListOf())

        // Create products with items
        categoryConfig.products.forEach { productConfig ->
          val product = productDataSource.createProduct(category.id, productConfig.name)

          // Add items to the product
          productConfig.items.forEach { itemConfig ->
            val newItem = NewItem(
              identifier = itemConfig.identifier,
              productId = product.id,
              expirationDate = itemConfig.expirationDate,
            )

            val itemId = itemDataSource.addItem(category.id, newItem)

            // Apply item-specific actions
            when {
              itemConfig.shouldFreeze -> {
                itemDataSource.freezeItem(itemId, remainingDays = itemConfig.remainingDaysWhenFrozen)
              }
              itemConfig.shouldConsume -> {
                itemDataSource.markItemAsConsumed(itemId)
              }
            }
          }
        }

        // Create standalone items (no product)
        categoryConfig.standaloneItems.forEach { standaloneConfig ->
          val newItem = NewItem(
            identifier = standaloneConfig.identifier,
            productId = null,
            expirationDate = standaloneConfig.expirationDate,
          )

          val itemId = itemDataSource.addItem(category.id, newItem)

          // Apply item-specific actions
          when {
            standaloneConfig.shouldFreeze -> {
              itemDataSource.freezeItem(itemId, remainingDays = standaloneConfig.remainingDaysWhenFrozen)
            }
            standaloneConfig.shouldConsume -> {
              itemDataSource.markItemAsConsumed(itemId)
            }
          }
        }
      }

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
