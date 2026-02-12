package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class NewItem(
  val identifier: String,
  val productId: String? = null,
  val expirationDate: Instant,
  val packSize: Int? = null,
)