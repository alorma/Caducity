package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.backup.BackupDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.NewItem
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID
import kotlin.time.Duration.Companion.days

/**
 * Use case to populate the database with fake data for testing
 *
 * Creates:
 * - 2 categories
 * - 2 products per category
 * - 6 items for each status (Fresh, ExpiringSoon, Expired, Frozen) per product
 * - 6 items for each status as standalone items (no product) per category
 *
 * Dates are calculated relative to today based on expiration thresholds
 */
class PopulateFakeDataUseCase(
  private val backupDataSource: BackupDataSource,
  private val categoryDataSource: CategoryDataSource,
  private val itemDataSource: ItemDataSource,
  private val productDataSource: ProductDataSource,
  private val expirationThresholds: ExpirationThresholds,
  private val appClock: AppClock,
) {

  suspend fun execute(): Result<Unit> {
    return try {
      // Clear all existing data first
      backupDataSource.clearAllData()

      val now = appClock.now()

      // Create categories
      val categories = listOf(
        Category(
          id = UUID.randomUUID().toString(),
          name = "Dairy",
          description = "Milk, cheese, yogurt and other dairy products"
        ),
        Category(
          id = UUID.randomUUID().toString(),
          name = "Produce",
          description = "Fresh fruits and vegetables"
        )
      )

      // Create items for each category
      categories.forEach { category ->
        // Create category first (empty)
        categoryDataSource.createCategory(category, persistentListOf())

        // Create standalone items (no product)
        val standaloneItems = createItemsForAllStatuses(
          productId = null,
          identifierPrefix = "Standalone",
          now = now
        )

        // Add standalone items and freeze the "Frozen" ones
        standaloneItems.forEach { newItem ->
          val itemId = itemDataSource.addItem(category.id, newItem)
          // Freeze items that have "Frozen" in their identifier
          if (newItem.identifier.contains("Frozen")) {
            itemDataSource.freezeItem(itemId, remainingDays = 5)
          }
        }

        // Create 2 products per category
        val productNames = when (category.name) {
          "Dairy" -> listOf("Whole Milk", "Cheddar Cheese")
          "Produce" -> listOf("Apples", "Carrots")
          else -> listOf("Product A", "Product B")
        }

        productNames.forEach { productName ->
          val product = productDataSource.createProduct(category.id, productName)

          // Create items for each status for this product
          val productItems = createItemsForAllStatuses(
            productId = product.id,
            identifierPrefix = productName,
            now = now
          )

          // Add each item to the category and apply status-specific actions
          productItems.forEach { newItem ->
            val itemId = itemDataSource.addItem(category.id, newItem)
            when {
              // Freeze items that have "Frozen" in their identifier
              newItem.identifier.contains("Frozen") -> {
                itemDataSource.freezeItem(itemId, remainingDays = 5)
              }
              // Consume items that have "Consumed" in their identifier
              newItem.identifier.contains("Consumed") -> {
                itemDataSource.markItemAsConsumed(itemId)
              }
            }
          }
        }
      }

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Creates 6 items for each status: Fresh, ExpiringSoon, Expired, Frozen, Consumed
   * Total: 30 items (6 per status)
   *
   * Note: Status is calculated by the ItemMapper based on expiration date.
   * Frozen status is achieved by calling freezeItem after creation.
   * Consumed status is achieved by calling markItemAsConsumed after creation.
   */
  private fun createItemsForAllStatuses(
    productId: String?,
    identifierPrefix: String,
    now: kotlin.time.Instant
  ): List<NewItem> {
    val items = mutableListOf<NewItem>()

    // Fresh items (beyond the soonExpiringThreshold)
    repeat(6) { index ->
      val daysAhead = expirationThresholds.soonExpiringThreshold.inWholeDays + 3 + index
      items.add(
        NewItem(
          identifier = "$identifierPrefix Fresh #${index + 1}",
          productId = productId,
          expirationDate = now.plus(daysAhead.days),
        )
      )
    }

    // ExpiringSoon items (within soonExpiringThreshold but not expired)
    repeat(6) { index ->
      val daysAhead = 1 + index // 1-6 days from now
      items.add(
        NewItem(
          identifier = "$identifierPrefix Expiring #${index + 1}",
          productId = productId,
          expirationDate = now.plus(daysAhead.days),
        )
      )
    }

    // Expired items (past expiration date)
    // 3 near expiration (less than consumeExpiredThreshold)
    // 3 far from expiration (more than consumeExpiredThreshold)
    val consumeThresholdDays = expirationThresholds.consumeExpiredThreshold.inWholeDays

    // Near expired (within consume threshold - can be consumed)
    repeat(3) { index ->
      items.add(
        NewItem(
          identifier = "$identifierPrefix Expired Near #${index + 1}",
          productId = productId,
          expirationDate = now.minus((index + 1).days), // 1-3 days ago
        )
      )
    }

    // Far expired (beyond consume threshold - cannot be consumed)
    repeat(3) { index ->
      items.add(
        NewItem(
          identifier = "$identifierPrefix Expired Far #${index + 1}",
          productId = productId,
          expirationDate = now.minus((consumeThresholdDays + index + 1).days),
        )
      )
    }

    // Frozen items (will be frozen after creation)
    repeat(6) { index ->
      items.add(
        NewItem(
          identifier = "$identifierPrefix Frozen #${index + 1}",
          productId = productId,
          expirationDate = now.plus((5 + index).days), // Future dates
        )
      )
    }

    // Consumed items (will be consumed after creation)
    // 3 near expiration (less than consumeExpiredThreshold)
    // 3 far from expiration (more than consumeExpiredThreshold)
    repeat(3) { index ->
      items.add(
        NewItem(
          identifier = "$identifierPrefix Consumed Near #${index + 1}",
          productId = productId,
          expirationDate = now.minus((index + 1).days), // 1-3 days ago (within threshold)
        )
      )
    }

    repeat(3) { index ->
      items.add(
        NewItem(
          identifier = "$identifierPrefix Consumed Far #${index + 1}",
          productId = productId,
          expirationDate = now.minus((consumeThresholdDays + index + 1).days), // Beyond threshold
        )
      )
    }

    return items
  }
}
