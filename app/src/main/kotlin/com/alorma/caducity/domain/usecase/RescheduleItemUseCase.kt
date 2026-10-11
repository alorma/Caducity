package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate

class RescheduleItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val pantryDataSource: PantryDataSource,
) {
  /** Gives all the units the same date in one transaction (bulk «Canvia la data»). */
  suspend fun reschedule(
    itemIds: List<String>,
    newDate: LocalDate,
  ): Result<Unit> =
    try {
      pantryDataSource.reschedule(itemIds, newDate.toExpirationInstant())
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

  suspend fun rescheduleItem(
    itemId: String,
    newExpirationDate: Instant,
  ): Result<Unit> {
    return try {
      // Verify item exists before rescheduling
      val item =
        itemDataSource.getItem(itemId)
          ?: return Result.failure(InstanceActionError.InstanceNotFound)

      itemDataSource.rescheduleItem(itemId, newExpirationDate.toEpochMilliseconds())
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
