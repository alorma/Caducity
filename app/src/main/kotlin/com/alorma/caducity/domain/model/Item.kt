package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class Item(
  val id: String,
  val identifier: String,
  val productId: String? = null,
  val expirationDate: Instant,
  val status: ItemStatus,
  val pausedDate: Instant? = null,
)