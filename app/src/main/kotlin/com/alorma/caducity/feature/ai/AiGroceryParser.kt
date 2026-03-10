package com.alorma.caducity.feature.ai

interface AiGroceryParser {
  suspend fun parse(
    input: String,
    todayIso: String,
  ): List<GroceryProposal>
}
