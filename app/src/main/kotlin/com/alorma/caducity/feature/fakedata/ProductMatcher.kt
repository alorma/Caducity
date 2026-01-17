package com.alorma.caducity.feature.fakedata

import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.feature.fakedata.models.GeneratedProduct
import com.alorma.caducity.feature.fakedata.models.MatchingResults
import com.alorma.caducity.feature.fakedata.models.ProductMatchResult

/**
 * Matches generated products against existing products in the database
 * Uses fuzzy string matching to detect similar product names
 */
class ProductMatcher {

  companion object {
    private const val PERFECT_MATCH_THRESHOLD = 0.75f
  }

  /**
   * Matches all generated products against existing products
   */
  fun matchProducts(
    generatedProducts: List<GeneratedProduct>,
    existingProducts: List<Product>,
  ): MatchingResults {
    val perfectMatches = mutableListOf<ProductMatchResult.PerfectMatch>()
    val noMatches = mutableListOf<ProductMatchResult.NoMatch>()

    generatedProducts.forEach { generatedProduct ->
      val matchResult = findBestMatch(generatedProduct, existingProducts)

      when {
        matchResult != null && matchResult.matchScore >= PERFECT_MATCH_THRESHOLD -> {
          perfectMatches.add(matchResult)
        }
        else -> {
          noMatches.add(ProductMatchResult.NoMatch(generatedProduct))
        }
      }
    }

    return MatchingResults(
      perfectMatches = perfectMatches,
      noMatches = noMatches
    )
  }

  /**
   * Finds the best matching existing product for a generated product
   */
  private fun findBestMatch(
    generatedProduct: GeneratedProduct,
    existingProducts: List<Product>,
  ): ProductMatchResult.PerfectMatch? {
    if (existingProducts.isEmpty()) return null

    val bestMatch = existingProducts
      .map { existing ->
        val score = calculateMatchScore(generatedProduct, existing)
        existing to score
      }
      .maxByOrNull { it.second }

    return bestMatch?.let { (product, score) ->
      ProductMatchResult.PerfectMatch(
        generatedProduct = generatedProduct,
        existingProduct = product,
        matchScore = score
      )
    }
  }

  /**
   * Calculates similarity score between generated and existing product
   * Returns 0.0 (no match) to 1.0 (perfect match)
   */
  private fun calculateMatchScore(
    generated: GeneratedProduct,
    existing: Product,
  ): Float {
    val nameScore = calculateStringSimilarity(
      generated.name.lowercase(),
      existing.name.lowercase()
    )

    // Weight name heavily (80%), description less (20%)
    val descriptionScore = if (existing.description.isNotBlank() && generated.description.isNotBlank()) {
      calculateStringSimilarity(
        generated.description.lowercase(),
        existing.description.lowercase()
      )
    } else {
      0f
    }

    return nameScore * 0.8f + descriptionScore * 0.2f
  }

  /**
   * Calculates string similarity using Levenshtein distance
   * Returns 0.0 (completely different) to 1.0 (identical)
   */
  private fun calculateStringSimilarity(s1: String, s2: String): Float {
    if (s1 == s2) return 1.0f
    if (s1.isEmpty() || s2.isEmpty()) return 0.0f

    // Check for substring match (common case: "Milk" matches "Whole Milk")
    if (s1.contains(s2) || s2.contains(s1)) {
      val longerLength = maxOf(s1.length, s2.length)
      val shorterLength = minOf(s1.length, s2.length)
      return shorterLength.toFloat() / longerLength.toFloat()
    }

    // Levenshtein distance for fuzzy matching
    val distance = levenshteinDistance(s1, s2)
    val maxLength = maxOf(s1.length, s2.length)
    return 1.0f - (distance.toFloat() / maxLength.toFloat())
  }

  /**
   * Calculates Levenshtein distance (edit distance) between two strings
   */
  private fun levenshteinDistance(s1: String, s2: String): Int {
    val len1 = s1.length
    val len2 = s2.length

    val dp = Array(len1 + 1) { IntArray(len2 + 1) }

    for (i in 0..len1) {
      dp[i][0] = i
    }
    for (j in 0..len2) {
      dp[0][j] = j
    }

    for (i in 1..len1) {
      for (j in 1..len2) {
        val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
        dp[i][j] = minOf(
          dp[i - 1][j] + 1,      // deletion
          dp[i][j - 1] + 1,      // insertion
          dp[i - 1][j - 1] + cost // substitution
        )
      }
    }

    return dp[len1][len2]
  }
}
