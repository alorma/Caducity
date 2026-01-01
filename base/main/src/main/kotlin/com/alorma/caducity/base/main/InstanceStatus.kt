package com.alorma.caducity.base.main

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

sealed class InstanceStatus {
  data object Expired : InstanceStatus()
  data object ExpiringSoon : InstanceStatus()
  data object Fresh : InstanceStatus()
  data object Frozen : InstanceStatus()
  data object Consumed : InstanceStatus()

  companion object {
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
    ): InstanceStatus {
      val today = now
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

      val expirationLocalDate = expirationDate
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

      val expiringSoonDate = now
        .plus(soonExpiringThreshold)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

      return when {
        expirationLocalDate <= today -> Expired
        expirationLocalDate < expiringSoonDate -> ExpiringSoon
        else -> Fresh
      }
    }
  }
}