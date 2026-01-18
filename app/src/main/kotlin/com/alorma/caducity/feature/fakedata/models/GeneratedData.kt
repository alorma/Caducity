package com.alorma.caducity.feature.fakedata.models

import kotlin.time.Duration

/**
 * Root data structure containing all generated grocery data from AI
 */
data class GeneratedGroceryData(
  val products: List<GeneratedProduct>,
)

/**
 * Represents a complete product with variants and standalone instances
 */
data class GeneratedProduct(
  val name: String,
  val description: String,
  val variants: List<GeneratedVariant>,
  val standaloneInstances: List<GeneratedInstance>,
)

/**
 * Represents a product variant (e.g., "Whole Milk 1L", "Skim Milk 500ml")
 */
data class GeneratedVariant(
  val name: String,
  val instances: List<GeneratedInstance>,
)

/**
 * Represents a single product instance with expiration info
 *
 * @param identifier Unique identifier (lot number, batch code, etc.)
 * @param daysFromNow Days until expiration (negative = expired, positive = future)
 */
data class GeneratedInstance(
  val identifier: String,
  val daysFromNow: Int,
) {
  /**
   * Converts days offset to Duration for use with AppClock
   */
  fun toDuration(): Duration = Duration.parse("${daysFromNow}d")
}

/**
 * Represents variants and instances generated for an existing product
 * Used when adding variants/instances to a product (not creating a new product)
 */
data class GeneratedProductVariants(
  val variants: List<GeneratedVariant>,
  val standaloneInstances: List<GeneratedInstance>,
)
