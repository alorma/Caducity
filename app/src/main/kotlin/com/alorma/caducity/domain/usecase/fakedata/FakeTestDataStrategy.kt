package com.alorma.caducity.domain.usecase.fakedata

import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Strategy for generating comprehensive test data with all item statuses
 *
 * Creates:
 * - 2 categories
 * - 2 products per category
 * - 6 items for each status (Fresh, ExpiringSoon, Expired, Frozen, Consumed) per product
 * - 6 items for each status as standalone items (no product) per category
 *
 * Dates are calculated relative to today based on expiration thresholds
 */
class FakeTestDataStrategy(
  private val expirationThresholds: ExpirationThresholds,
) : FakeDataStrategy {
  override fun getCategoryConfigs(now: Instant): List<CategoryConfig> =
    listOf(
      createDairyCategory(now),
      createProduceCategory(now),
    )

  private fun createDairyCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Dairy",
      description = "Milk, cheese, yogurt and other dairy products",
      products =
        listOf(
          createProductWithAllStatuses("Whole Milk", now),
          createProductWithAllStatuses("Cheddar Cheese", now),
        ),
      standaloneItems = createStandaloneItemsWithAllStatuses("Standalone", now),
    )

  private fun createProduceCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Produce",
      description = "Fresh fruits and vegetables",
      products =
        listOf(
          createProductWithAllStatuses("Apples", now),
          createProductWithAllStatuses("Carrots", now),
        ),
      standaloneItems = createStandaloneItemsWithAllStatuses("Standalone", now),
    )

  /**
   * Creates a product with 6 items for each status: Fresh, ExpiringSoon, Expired, Frozen, Consumed
   * Total: 30 items (6 per status)
   */
  private fun createProductWithAllStatuses(
    productName: String,
    now: Instant,
  ): ProductConfig {
    val items = mutableListOf<ItemConfig>()

    // Fresh items (beyond the soonExpiringThreshold)
    repeat(6) { index ->
      val daysAhead = expirationThresholds.soonExpiringThreshold.inWholeDays + 3 + index
      items.add(
        ItemConfig(
          identifier = "$productName Fresh #${index + 1}",
          expirationDate = now.plus(daysAhead.days),
        ),
      )
    }

    // ExpiringSoon items (within soonExpiringThreshold but not expired)
    repeat(6) { index ->
      val daysAhead = 1 + index // 1-6 days from now
      items.add(
        ItemConfig(
          identifier = "$productName Expiring #${index + 1}",
          expirationDate = now.plus(daysAhead.days),
        ),
      )
    }

    // Expired items (past expiration date)
    val consumeThresholdDays = expirationThresholds.consumeExpiredThreshold.inWholeDays

    // Near expired (within consume threshold - can be consumed)
    repeat(3) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Expired Near #${index + 1}",
          expirationDate = now.minus((index + 1).days), // 1-3 days ago
        ),
      )
    }

    // Far expired (beyond consume threshold - cannot be consumed)
    repeat(3) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Expired Far #${index + 1}",
          expirationDate = now.minus((consumeThresholdDays + index + 1).days),
        ),
      )
    }

    // Frozen items (will be frozen after creation)
    repeat(6) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Frozen #${index + 1}",
          expirationDate = now.plus((5 + index).days),
          shouldFreeze = true,
          remainingDaysWhenFrozen = 5,
        ),
      )
    }

    // Consumed items (will be consumed after creation)
    // Near expiration (within consumeExpiredThreshold)
    repeat(3) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Consumed Near #${index + 1}",
          expirationDate = now.minus((index + 1).days), // 1-3 days ago
          shouldConsume = true,
        ),
      )
    }

    // Far from expiration (beyond consumeExpiredThreshold)
    repeat(3) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Consumed Far #${index + 1}",
          expirationDate = now.minus((consumeThresholdDays + index + 1).days),
          shouldConsume = true,
        ),
      )
    }

    return ProductConfig(name = productName, items = items)
  }

  /**
   * Creates standalone items (no product) with all statuses
   */
  private fun createStandaloneItemsWithAllStatuses(
    prefix: String,
    now: Instant,
  ): List<StandaloneItemConfig> {
    val items = mutableListOf<StandaloneItemConfig>()
    val consumeThresholdDays = expirationThresholds.consumeExpiredThreshold.inWholeDays

    // Fresh items
    repeat(6) { index ->
      val daysAhead = expirationThresholds.soonExpiringThreshold.inWholeDays + 3 + index
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Fresh #${index + 1}",
          expirationDate = now.plus(daysAhead.days),
        ),
      )
    }

    // ExpiringSoon items
    repeat(6) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Expiring #${index + 1}",
          expirationDate = now.plus((1 + index).days),
        ),
      )
    }

    // Expired items (near)
    repeat(3) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Expired Near #${index + 1}",
          expirationDate = now.minus((index + 1).days),
        ),
      )
    }

    // Expired items (far)
    repeat(3) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Expired Far #${index + 1}",
          expirationDate = now.minus((consumeThresholdDays + index + 1).days),
        ),
      )
    }

    // Frozen items
    repeat(6) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Frozen #${index + 1}",
          expirationDate = now.plus((5 + index).days),
          shouldFreeze = true,
          remainingDaysWhenFrozen = 5,
        ),
      )
    }

    // Consumed items (near)
    repeat(3) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Consumed Near #${index + 1}",
          expirationDate = now.minus((index + 1).days),
          shouldConsume = true,
        ),
      )
    }

    // Consumed items (far)
    repeat(3) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Consumed Far #${index + 1}",
          expirationDate = now.minus((consumeThresholdDays + index + 1).days),
          shouldConsume = true,
        ),
      )
    }

    return items
  }
}
