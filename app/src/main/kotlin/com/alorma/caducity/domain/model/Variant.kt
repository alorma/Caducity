package com.alorma.caducity.domain.model

data class Variant(
  val id: String,
  val productId: String,
  val name: String,
  val createdAt: kotlin.time.Instant,
)
