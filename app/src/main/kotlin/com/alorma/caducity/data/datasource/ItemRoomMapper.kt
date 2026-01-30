package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.ItemRoomEntity
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlin.time.Instant

class ItemRoomMapper(
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds
) {

  fun toModel(entity: ItemRoomEntity): ProductInstance {
    return ProductInstance(
      id = entity.id,
      identifier = entity.identifier,
      variantId = entity.productId,
      expirationDate = instantFromTimestamp(entity.expirationDate),
      status = instanceStatus(entity),
      pausedDate = entity.pausedDate?.let { date -> instantFromTimestamp(date) },
    )
  }

  private fun instanceStatus(
    entity: ItemRoomEntity,
  ): InstanceStatus {
    // Note: consumedDate items are filtered at SQL level, so they never reach this mapper
    return when {
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