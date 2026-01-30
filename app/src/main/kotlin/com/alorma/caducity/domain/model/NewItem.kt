package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class NewItem(
  val identifier: String,
  val categoryId: String? = null,
  val expirationDate: Instant,
)