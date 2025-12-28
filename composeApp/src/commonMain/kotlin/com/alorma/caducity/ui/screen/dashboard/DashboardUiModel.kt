package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

@Stable
sealed interface ProductUiModel {
  val id: String
  val name: String
  val description: String

  @Stable
  data class WithInstances(
    override val id: String,
    override val name: String,
    override val description: String,
    val today: String,
    val instances: ImmutableList<ProductInstanceUiModel>,
  ) : ProductUiModel

  @Stable
  data class Empty(
    override val id: String,
    override val name: String,
    override val description: String,
  ) : ProductUiModel
}

@Stable
data class ProductInstanceUiModel(
  val id: String,
  val identifier: String,
  val status: InstanceStatus,
  val expirationDate: String,
)

sealed class InstanceStatus {
  data object Expired : InstanceStatus()
  data object ExpiringSoon : InstanceStatus()
  data object Fresh : InstanceStatus()

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