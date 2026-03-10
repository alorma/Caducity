package com.alorma.caducity.feature.ai

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.Product

sealed interface MatchResult {
  data class Match(val product: Product, val category: Category, val score: Double) : MatchResult
  data class CategoryMatch(val category: Category) : MatchResult
  data object NoMatch : MatchResult
}

val MatchResult.priority: Int get() = when (this) {
  is MatchResult.Match -> 0
  is MatchResult.CategoryMatch -> 1
  MatchResult.NoMatch -> 2
}

interface AiProductMatcher {
  suspend fun match(proposal: GroceryProposal): MatchResult
}
