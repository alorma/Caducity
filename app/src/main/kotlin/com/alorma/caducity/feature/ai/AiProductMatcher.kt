package com.alorma.caducity.feature.ai

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.Product

sealed interface MatchResult {
  data class Match(val product: Product, val category: Category, val score: Double) : MatchResult
  data object NoMatch : MatchResult
}

interface AiProductMatcher {
  suspend fun match(proposal: GroceryProposal): MatchResult
}
