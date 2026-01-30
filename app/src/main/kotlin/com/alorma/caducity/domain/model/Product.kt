package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class Product(
  val id: String,
  val categoryId: String,
  val name: String,
  val createdAt: Instant,
)
