package com.alorma.caducity.domain.model

import kotlinx.datetime.Instant

data class Variant(
  val id: String,
  val productId: String,
  val name: String,
  val createdAt: Instant,
)
