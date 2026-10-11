package com.alorma.caducity.domain.usecase.fakedata

import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import kotlin.time.Instant

/**
 * Strategy interface for generating fake data
 * Different strategies can create different data sets for different purposes
 */
interface FakeDataStrategy {
  /**
   * Get the list of category configurations to create
   */
  fun getCategoryConfigs(now: Instant): List<CategoryConfig>
}

/**
 * Configuration for a category
 */
data class CategoryConfig(
  val name: String,
  val description: String,
  val products: List<ProductConfig>,
  val standaloneItems: List<StandaloneItemConfig> = emptyList(),
  /** When null, shape/icon are derived from the name like for migrated categories. */
  val shape: CategoryShape? = null,
  val icon: CategoryIcon? = null,
  /** Own "soon" margin in days; null follows the global default. */
  val soonThresholdDays: Int? = null,
)

/**
 * Configuration for a product
 */
data class ProductConfig(
  val name: String,
  val items: List<ItemConfig>,
)

/**
 * Configuration for an item within a product
 */
data class ItemConfig(
  val identifier: String,
  val expirationDate: Instant,
  val shouldFreeze: Boolean = false,
  val remainingDaysWhenFrozen: Int = 5,
  val shouldConsume: Boolean = false,
  /** Legacy pack (`> 1`) to exercise the partial-consume dialog; new items are always independent units. */
  val packSize: Int? = null,
)

/**
 * Configuration for a standalone item (no product)
 */
data class StandaloneItemConfig(
  val identifier: String,
  val expirationDate: Instant,
  val shouldFreeze: Boolean = false,
  val remainingDaysWhenFrozen: Int = 5,
  val shouldConsume: Boolean = false,
)
