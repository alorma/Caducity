package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Visual state of a product: determines its color and label. */
enum class ExpiryStatus {
  EXPIRED,
  SOON,
  FRESH,
  FROZEN,
  ;

  companion object {
    /**
     * Pure state computation: `FROZEN` wins; `EXPIRED` if `expiry < today`;
     * `SOON` if `today <= expiry <= today + thresholdDays`; otherwise `FRESH`.
     */
    fun of(
      expiry: LocalDate,
      today: LocalDate,
      thresholdDays: Int,
      frozen: Boolean,
    ): ExpiryStatus {
      val daysLeft = today.daysUntil(expiry)
      return when {
        frozen -> FROZEN
        daysLeft < 0 -> EXPIRED
        daysLeft <= thresholdDays -> SOON
        else -> FRESH
      }
    }
  }
}
