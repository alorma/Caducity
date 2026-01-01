package com.alorma.caducity.data.entity

data class ProductEntity(
  val id: String,
  val name: String,
  val description: String,
)

data class ProductInstanceEntity(
  val id: String,
  val productId: String,
  val expirationDate: Long,
  val purchaseDate: Long,
)
