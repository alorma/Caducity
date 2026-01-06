package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class NewProductInstance(
  val identifier: String,
  val variantId: String? = null,
  val expirationDate: Instant,
)