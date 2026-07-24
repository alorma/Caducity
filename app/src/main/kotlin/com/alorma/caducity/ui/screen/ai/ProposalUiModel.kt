package com.alorma.caducity.ui.screen.ai

import com.alorma.caducity.feature.ai.GroceryProposal
import com.alorma.caducity.feature.ai.MatchResult

data class ProposalUiModel(
  val id: String,
  val proposal: GroceryProposal,
  val matchResult: MatchResult,
  val done: Boolean = false,
)
