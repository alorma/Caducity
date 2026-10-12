package com.alorma.caducity.data.datasource.room.mapper

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.NewItem
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlin.time.Instant

/**
 * Mapper for Item entity <-> domain model conversions.
 *
 * Responsibilities:
 * - Map ItemRoomEntity to Item domain model with status calculation
 * - Map Item domain model to ItemRoomEntity
 * - Map NewItem to ItemRoomEntity (for item creation)
 * - Handle timestamp conversions for dates
 * - Calculate item status based on expiration, frozen, and consumed states
 */
class ItemRoomMapper(
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {
  /**
   * Maps ItemRoomEntity to Item domain model with calculated status
   */
  fun toModel(entity: ItemRoomEntity): Item =
    Item(
      id = entity.id,
      categoryId = entity.categoryId,
      identifier = entity.identifier,
      productId = entity.productId,
      expirationDate = Instant.fromEpochMilliseconds(entity.expirationDate),
      status = calculateStatus(entity),
      pausedDate = entity.pausedDate?.let { Instant.fromEpochMilliseconds(it) },
      remainingDays = entity.remainingDays,
      packSize = entity.packSize,
      discardedDate = entity.discardedDate?.let { Instant.fromEpochMilliseconds(it) },
      createdAt = entity.createdAt?.let { Instant.fromEpochMilliseconds(it) },
    )

  /**
   * Maps Item domain model to ItemRoomEntity
   */
  fun toEntity(model: Item): ItemRoomEntity =
    ItemRoomEntity(
      id = model.id,
      categoryId = model.categoryId,
      identifier = model.identifier,
      productId = model.productId,
      expirationDate = model.expirationDate.toEpochMilliseconds(),
      pausedDate = model.pausedDate?.toEpochMilliseconds(),
      remainingDays = null,
      consumedDate = null,
      packSize = model.packSize,
      discardedDate = model.discardedDate?.toEpochMilliseconds(),
      createdAt = model.createdAt?.toEpochMilliseconds(),
    )

  /**
   * Maps NewItem to ItemRoomEntity (for item creation)
   *
   * @param model The NewItem to convert
   * @param id The generated ID for this new item
   * @param categoryId The category ID this item belongs to
   */
  fun toEntity(
    model: NewItem,
    id: String,
    categoryId: String,
  ): ItemRoomEntity =
    ItemRoomEntity(
      id = id,
      categoryId = categoryId,
      identifier = model.identifier,
      productId = model.productId,
      expirationDate = model.expirationDate.toEpochMilliseconds(),
      pausedDate = null,
      remainingDays = null,
      consumedDate = null,
      packSize = model.packSize,
      createdAt = appClock.now().toEpochMilliseconds(),
    )

  /**
   * Calculates item status based on consumed, frozen, and expiration states
   *
   * Priority: Consumed > Discarded > Frozen > Calculated (based on expiration)
   *
   * TEMP(fase 5): the new Observe* use cases compute status from `ExpiryStatus.of`; old screens still read this.
   */
  private fun calculateStatus(entity: ItemRoomEntity): ItemStatus =
    when {
      entity.consumedDate != null -> ItemStatus.Consumed
      // A transformed ingredient is out of the pantry like a consumed one (no extra status needed for legacy readers).
      entity.transformedDate != null -> ItemStatus.Consumed
      entity.discardedDate != null -> ItemStatus.Discarded
      entity.pausedDate != null -> ItemStatus.Frozen
      else ->
        ItemStatus.calculateStatus(
          expirationDate = Instant.fromEpochMilliseconds(entity.expirationDate),
          now = appClock.now(),
          soonExpiringThreshold = expirationThresholds.soonExpiringThreshold,
        )
    }
}
