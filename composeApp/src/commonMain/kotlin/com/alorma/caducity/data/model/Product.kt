package com.alorma.caducity.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
  val id: String,
  val name: String,
  val description: String,
  val expirationDate: Long, // Timestamp in milliseconds
  val isExpired: Boolean = false,
)
