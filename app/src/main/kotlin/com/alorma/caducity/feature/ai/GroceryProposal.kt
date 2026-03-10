package com.alorma.caducity.feature.ai

data class GroceryProposal(
  val productName: String,
  val quantity: Int,
  val expirationDate: String, // ISO-8601 date: YYYY-MM-DD
  val category: String,
)
