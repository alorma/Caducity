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
      name = "Làctics",
      description = "Llet, formatge, iogurt i altres productes làctics",
      usualDurationDays = 7,
      products =
        listOf(
          createProductWithAllStatuses("Llet sencera", now),
          createProductWithAllStatuses("Formatge cheddar", now),
        ),
      standaloneItems = createStandaloneItemsWithAllStatuses("Solt", now),
    )

  private fun createProduceCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Fruita i verdura",
      description = "Fruita i verdura fresques",
      usualDurationDays = 7,
      products =
        listOf(
          createProductWithAllStatuses("Pomes", now),
          createProductWithAllStatuses("Pastanagues", now),
        ),
      standaloneItems = createStandaloneItemsWithAllStatuses("Solt", now),
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
          identifier = "$productName Fresc #${index + 1}",
          expirationDate = now.plus(daysAhead.days),
        ),
      )
    }

    // ExpiringSoon items (within soonExpiringThreshold but not expired)
    repeat(6) { index ->
      val daysAhead = 1 + index // 1-6 days from now
      items.add(
        ItemConfig(
          identifier = "$productName Caduca aviat #${index + 1}",
          expirationDate = now.plus(daysAhead.days),
        ),
      )
    }

    // Expired items (past expiration date)
    val consumeThresholdDays = CONSUME_THRESHOLD_DAYS

    // Near expired (within consume threshold - can be consumed)
    repeat(3) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Caducat recent #${index + 1}",
          expirationDate = now.minus((index + 1).days), // 1-3 days ago
        ),
      )
    }

    // Far expired (beyond consume threshold - cannot be consumed)
    repeat(3) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Caducat fa temps #${index + 1}",
          expirationDate = now.minus((consumeThresholdDays + index + 1).days),
        ),
      )
    }

    // Frozen items (will be frozen after creation)
    repeat(6) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Congelat #${index + 1}",
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
          identifier = "$productName Consumit recent #${index + 1}",
          expirationDate = now.minus((index + 1).days), // 1-3 days ago
          shouldConsume = true,
        ),
      )
    }

    // Far from expiration (beyond consumeExpiredThreshold)
    repeat(3) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Consumit fa temps #${index + 1}",
          expirationDate = now.minus((consumeThresholdDays + index + 1).days),
          shouldConsume = true,
        ),
      )
    }

    // Transformed ingredients: must not show up anywhere (they would otherwise be fresh)
    repeat(3) { index ->
      items.add(
        ItemConfig(
          identifier = "$productName Transformat #${index + 1}",
          expirationDate = now.plus((10 + index).days),
          shouldTransform = true,
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
    val consumeThresholdDays = CONSUME_THRESHOLD_DAYS

    // Fresh items
    repeat(6) { index ->
      val daysAhead = expirationThresholds.soonExpiringThreshold.inWholeDays + 3 + index
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Fresc #${index + 1}",
          expirationDate = now.plus(daysAhead.days),
        ),
      )
    }

    // ExpiringSoon items
    repeat(6) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Caduca aviat #${index + 1}",
          expirationDate = now.plus((1 + index).days),
        ),
      )
    }

    // Expired items (near)
    repeat(3) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Caducat recent #${index + 1}",
          expirationDate = now.minus((index + 1).days),
        ),
      )
    }

    // Expired items (far)
    repeat(3) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Caducat fa temps #${index + 1}",
          expirationDate = now.minus((consumeThresholdDays + index + 1).days),
        ),
      )
    }

    // Frozen items
    repeat(6) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Congelat #${index + 1}",
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
          identifier = "$prefix Consumit recent #${index + 1}",
          expirationDate = now.minus((index + 1).days),
          shouldConsume = true,
        ),
      )
    }

    // Consumed items (far)
    repeat(3) { index ->
      items.add(
        StandaloneItemConfig(
          identifier = "$prefix Consumit fa temps #${index + 1}",
          expirationDate = now.minus((consumeThresholdDays + index + 1).days),
          shouldConsume = true,
        ),
      )
    }

    return items
  }

  private companion object {
    /** Days before today that still count as «recently expired» in the fake data. */
    const val CONSUME_THRESHOLD_DAYS = 2L
  }
}
