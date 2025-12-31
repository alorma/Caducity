package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class ProductInstance(
  val id: String,
  val identifier: String,
  val expirationDate: Instant,
  val status: InstanceStatus,
)
