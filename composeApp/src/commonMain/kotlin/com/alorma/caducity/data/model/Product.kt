package com.alorma.caducity.data.model

import kotlin.time.Instant

data class Product(
  val id: String,
  val name: String,
  val description: String,
)

data class ProductInstance(
  val id: String,
  val expirationDate: Instant,
  val purchaseDate: Instant,
)
