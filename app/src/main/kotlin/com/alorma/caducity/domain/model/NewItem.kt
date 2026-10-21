package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class NewItem(
  val identifier: String,
  val productId: String? = null,
  val expirationDate: Instant,
  /** g or mL of a measured product; null for units. */
  val amount: Int? = null,
)
