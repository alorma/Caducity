package com.alorma.caducity.domain.model

import com.alorma.caducity.config.time.date
import kotlin.time.Duration
import kotlin.time.Instant

sealed class ItemStatus {
  data object Expired : ItemStatus()
  data object ExpiringSoon : ItemStatus()
  data object Fresh : ItemStatus()
  data object Frozen : ItemStatus()
  data object Consumed : ItemStatus()

  companion object Companion {
    /**
     * Calculates the expiration status for a product instance.
     *
     * @param expirationDate The expiration date as an Instant
     * @param now The current time as an Instant
     * @param soonExpiringThreshold The threshold duration for "expiring soon" status
     * @return The calculated InstanceStatus
     */
    fun calculateStatus(
      expirationDate: Instant,
      now: Instant,
      soonExpiringThreshold: Duration
    ): ItemStatus {
      val today = now.date()
      val expirationLocalDate = expirationDate.date()
      val expiringSoonDate = now.plus(soonExpiringThreshold).date()

      return when {
        expirationLocalDate < today -> Expired
        expirationLocalDate < expiringSoonDate -> ExpiringSoon
        else -> Fresh
      }
    }
  }
}