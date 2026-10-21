package com.alorma.caducity.domain.usecase.fakedata

import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.UnitType
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
  /** Usual duration of this kind of food in days; null means none. */
  val usualDurationDays: Int? = null,
)

/**
 * Configuration for a product
 */
data class ProductConfig(
  val name: String,
  val items: List<ItemConfig>,
  val unitType: UnitType = UnitType.DEFAULT,
)

/**
 * Configuration for an item within a product
 */
data class ItemConfig(
  val identifier: String,
  val expirationDate: Instant,
  val shouldFreeze: Boolean = false,
  val remainingDaysWhenFrozen: Int = 5,
  /** Days since it went into the freezer (0 = today). */
  val frozenDaysAgo: Int = 0,
  val shouldConsume: Boolean = false,
  /** Used as an ingredient of a Transforma: must disappear from every live view. */
  val shouldTransform: Boolean = false,
  /** g or mL of a measured product (single batch); null for units. */
  val amount: Int? = null,
  /** Level of a handful product (a single batch); null for the other types. */
  val level: HandfulLevel? = null,
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
