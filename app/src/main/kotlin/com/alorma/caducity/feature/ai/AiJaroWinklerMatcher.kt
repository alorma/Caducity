package com.alorma.caducity.feature.ai

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.Product
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

    for (categoryWithItems in categories) {
      val category = categoryWithItems.category
      val categoryScore =
        jaroWinkler(
          proposal.category.lowercase(),
          category.name.lowercase(),
        )
      if (categoryScore > bestCategoryScore) {
        bestCategoryScore = categoryScore
        bestCategoryOnly = category
      }
      for (categoryProduct in categoryWithItems.products) {
        val product = categoryProduct.product
        val proposalWords = proposal.productName.lowercase().split(" ")
        val productWords = product.name.lowercase().split(" ")
        // First-word gate: the leading word must be similar enough.
        // This prevents "apple juice" matching "orange juice" via shared suffix.
        val firstWordScore = jaroWinkler(proposalWords.first(), productWords.first())
        if (firstWordScore < 0.80) continue

        val productScore =
          jaroWinkler(
            proposal.productName.lowercase(),
            product.name.lowercase(),
          )
        val score = (categoryScore + productScore) / 2.0
        Timber.tag("AiMatcher").d(
          "proposal='%s/%s' vs '%s/%s' → cat=%.2f prod=%.2f first=%.2f avg=%.2f",
          proposal.category,
          proposal.productName,
          category.name,
          product.name,
          categoryScore,
          productScore,
          firstWordScore,
          score,
        )
        if (score > bestScore) {
          bestScore = score
          bestProductScore = productScore
          bestProduct = product
          bestCategory = category
        }
      }
    }

    val result =
      if (bestScore >= 0.75 && bestProduct != null && bestCategory != null && bestProductScore >= 0.75) {
        MatchResult.Match(product = bestProduct, category = bestCategory, score = bestScore)
      } else if (bestCategoryScore >= 0.75 && bestCategoryOnly != null) {
        MatchResult.CategoryMatch(category = bestCategoryOnly)
      } else {
        MatchResult.NoMatch
      }
    Timber.tag("AiMatcher").d("Best match for '%s': %s (score=%.2f)", proposal.productName, result, bestScore)
    return result
  }
}
