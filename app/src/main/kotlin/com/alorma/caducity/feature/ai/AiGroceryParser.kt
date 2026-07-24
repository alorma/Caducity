package com.alorma.caducity.feature.ai

interface AiGroceryParser {
  suspend fun parse(
    input: String,
    existingCategories: List<String>,
  ): GroceryParseResult
}
