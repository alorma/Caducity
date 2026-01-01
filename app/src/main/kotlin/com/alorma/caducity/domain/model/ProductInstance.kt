package com.alorma.caducity.domain.model

import com.alorma.caducity.base.main.InstanceStatus
import kotlin.time.Instant

data class ProductInstance(
  val id: String,
  val identifier: String,
  val expirationDate: Instant,
  val status: InstanceStatus,
  val pausedDate: Instant? = null,
) {
  /**
   * Returns the date that should be used for display purposes:
   * - For frozen items: the pausedDate (when it was frozen)
   * - For all other items: the expirationDate
   */
  val displayDate: Instant
    get() = if (status == InstanceStatus.Frozen && pausedDate != null) {
      pausedDate
    } else {
      expirationDate
    }
}
