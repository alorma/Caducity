package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.ProductInstanceRoomEntity
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlin.time.Instant

class InstanceRoomMapper(
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds
) {

  fun toModel(entity: ProductInstanceRoomEntity): ProductInstance {
    return ProductInstance(
      id = entity.id,
      identifier = entity.identifier,
      variantId = entity.variantId,
      expirationDate = instantFromTimestamp(entity.expirationDate),
      status = instanceStatus(entity),
      pausedDate = entity.pausedDate?.let { date -> instantFromTimestamp(date) },
    )
  }

  private fun instanceStatus(
    entity: ProductInstanceRoomEntity,
  ): InstanceStatus {
    return when {
      entity.consumedDate != null -> InstanceStatus.Consumed
      entity.pausedDate != null -> InstanceStatus.Frozen
      else -> InstanceStatus.calculateStatus(
        expirationDate = instantFromTimestamp(entity.expirationDate),
        now = appClock.now(),
        soonExpiringThreshold = expirationThresholds.soonExpiringThreshold
      )
    }
  }

  private fun instantFromTimestamp(timestamp: Long): Instant =
    Instant.fromEpochMilliseconds(timestamp)
}