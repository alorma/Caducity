package com.alorma.caducity.feature.fakedata.models

import com.alorma.caducity.domain.model.Product

/**
 * Represents the result of matching generated products against existing products
 */
sealed class ProductMatchResult {
  /**
   * Generated product perfectly matches an existing product
   * User should review before adding variants/instances
   */
  data class PerfectMatch(
    val generatedProduct: GeneratedProduct,
    val existingProduct: Product,
    val matchScore: Float, // 0.0 to 1.0
  ) : ProductMatchResult()

  /**
   * No good match found - safe to create as new product
   */
  data class NoMatch(
    val generatedProduct: GeneratedProduct,
  ) : ProductMatchResult()
}

/**
 * Results from matching all generated products
 */
data class MatchingResults(
  val perfectMatches: List<ProductMatchResult.PerfectMatch>,
  val noMatches: List<ProductMatchResult.NoMatch>,
) {
  val hasMatches: Boolean
    get() = perfectMatches.isNotEmpty()

  val shouldReview: Boolean
    get() = perfectMatches.isNotEmpty()
}
