package com.alorma.caducity.domain.model

import kotlin.time.Instant

data class Item(
  val id: String,
  val categoryId: String,
  val identifier: String,
  val productId: String? = null,
  val expirationDate: Instant,
  val status: ItemStatus,
  val pausedDate: Instant? = null,
  val remainingDays: Int? = null,
  val discardedDate: Instant? = null,
  val createdAt: Instant? = null,
) {
  /** An item is active until it is consumed or discarded. */
  val isActive: Boolean
    get() = status != ItemStatus.Consumed && status != ItemStatus.Discarded
}
