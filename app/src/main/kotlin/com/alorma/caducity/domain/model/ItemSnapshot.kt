package com.alorma.caducity.domain.model

/** The state fields an action can change on one item, to undo bulk actions exactly (also for frozen units). */
data class ItemSnapshot(
  val itemId: String,
  val expirationMillis: Long,
  val pausedMillis: Long?,
  val remainingDays: Int?,
  val consumedMillis: Long?,
  val discardedMillis: Long?,
)
