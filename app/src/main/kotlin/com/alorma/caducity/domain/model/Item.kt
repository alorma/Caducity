package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class Item(
  val id: String,
  val identifier: String,
  val categoryId: String? = null,
  val expirationDate: Instant,
  val status: InstanceStatus,
  val pausedDate: Instant? = null,
)