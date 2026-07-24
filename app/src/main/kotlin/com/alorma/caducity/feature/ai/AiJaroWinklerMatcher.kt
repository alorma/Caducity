package com.alorma.caducity.feature.ai

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.Product
import java.text.Normalizer
import kotlinx.coroutines.flow.first
import timber.log.Timber

class AiJaroWinklerMatcher(
  private val categoryDataSource: CategoryDataSource,
) : AiProductMatcher {
  override suspend fun match(proposal: GroceryProposal): MatchResult {
    val categories = categoryDataSource.getCategories().first()
    var bestScore = 0.0
    var bestProductScore = 0.0
    var bestProduct: Product? = null
    var bestCategory: Category? = null
    var bestCategoryScore = 0.0
    var bestCategoryOnly: Category? = null

    val proposalCategory = normalize(proposal.category)

    for (categoryWithItems in categories) {
      val category = categoryWithItems.category
      val categoryScore = jaroWinkler(proposalCategory, normalize(category.name))
      if (categoryScore > bestCategoryScore) {
        bestCategoryScore = categoryScore
        bestCategoryOnly = category
      }
      for (categoryProduct in categoryWithItems.products) {
        val product = categoryProduct.product
        // Token-aware similarity handles word order and subsets like
        // "whole milk" vs "milk", while still rejecting "apple juice" vs
        // "orange juice" (the distinctive token stays uncovered).
        val productScore = productSimilarity(proposal.productName, product.name)
        if (productScore <= 0.0) continue

        val score = (categoryScore + productScore) / 2.0
        if (score > bestScore) {
          bestScore = score
          bestProductScore = productScore
          bestProduct = product
          bestCategory = category
        }
      }
    }

    val result =
      when {
        bestProduct != null &&
          bestCategory != null &&
          bestScore >= SCORE_THRESHOLD &&
          bestProductScore >= PRODUCT_THRESHOLD ->
          MatchResult.Match(product = bestProduct, category = bestCategory, score = bestScore)

        bestCategoryOnly != null && bestCategoryScore >= SCORE_THRESHOLD ->
          MatchResult.CategoryMatch(category = bestCategoryOnly)

        else -> MatchResult.NoMatch
      }
    Timber.tag("AiMatcher").d("Best match for '%s': %s (score=%.2f)", proposal.productName, result, bestScore)
    return result
  }

  companion object {
    private const val SCORE_THRESHOLD = 0.75
    private const val PRODUCT_THRESHOLD = 0.75
    private const val TOKEN_MATCH_THRESHOLD = 0.80

    /** Lowercases, trims and strips diacritics so "café" matches "cafe". */
    internal fun normalize(value: String): String =
      Normalizer
        .normalize(value.lowercase().trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

    /**
     * Similarity between two product names based on token coverage.
     *
     * Every token of the shorter name must find a token in the longer name
     * scoring at least [TOKEN_MATCH_THRESHOLD], and the head noun (last token)
     * of the longer name must be covered too; otherwise the names are treated
     * as unrelated (0.0). The head-noun rule lets "whole milk" match "milk"
     * while keeping "milk" from matching "milk chocolate". The returned score
     * is the average of the shorter name's best per-token matches.
     */
    internal fun productSimilarity(
      a: String,
      b: String,
    ): Double {
      val normA = normalize(a)
      val normB = normalize(b)
      if (normA == normB) return 1.0

      val tokensA = normA.split(" ").filter { it.isNotBlank() }
      val tokensB = normB.split(" ").filter { it.isNotBlank() }
      if (tokensA.isEmpty() || tokensB.isEmpty()) return 0.0

      val (shorter, longer) = if (tokensA.size <= tokensB.size) tokensA to tokensB else tokensB to tokensA

      val headCovered = shorter.any { jaroWinkler(it, longer.last()) >= TOKEN_MATCH_THRESHOLD }
      if (!headCovered) return 0.0

      var sum = 0.0
      for (token in shorter) {
        val best = longer.maxOf { jaroWinkler(token, it) }
        if (best < TOKEN_MATCH_THRESHOLD) return 0.0
        sum += best
      }
      return sum / shorter.size
    }
  }
}
