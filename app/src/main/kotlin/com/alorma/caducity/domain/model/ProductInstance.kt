package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class ProductInstance(
  val id: String,
  val identifier: String,
  val variantId: String? = null,
  val expirationDate: Instant,
  val status: InstanceStatus,
  val pausedDate: Instant? = null,
)