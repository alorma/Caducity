package com.alorma.caducity.data.model

import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import kotlin.time.Instant

data class Product(
  val id: String,
  val name: String,
  val description: String,
)

data class ProductInstance(
  val id: String,
  val identifier: String,
  val expirationDate: Instant,
  val status: InstanceStatus,
)
