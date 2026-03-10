package com.alorma.caducity.feature.ai

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.Product
import kotlinx.coroutines.flow.first

class AiJaroWinklerMatcher(
  private val categoryDataSource: CategoryDataSource,
) : AiProductMatcher {
  override suspend fun match(proposal: GroceryProposal): MatchResult {
    val categories = categoryDataSource.getCategories().first()
    var bestScore = 0.0
    var bestProduct: Product? = null
    var bestCategory: Category? = null

    for (categoryWithItems in categories) {
      val category = categoryWithItems.category
      val categoryScore = jaroWinkler(
        proposal.category.lowercase(),
        category.name.lowercase(),
      )
      for (categoryProduct in categoryWithItems.products) {
        val product = categoryProduct.product
        val productScore = jaroWinkler(
          proposal.productName.lowercase(),
          product.name.lowercase(),
        )
        val score = (categoryScore + productScore) / 2.0
        if (score > bestScore) {
          bestScore = score
          bestProduct = product
          bestCategory = category
        }
      }
    }

    return if (bestScore >= 0.50 && bestProduct != null && bestCategory != null) {
      MatchResult.Match(product = bestProduct, category = bestCategory, score = bestScore)
    } else {
      MatchResult.NoMatch
    }
  }
}
