package com.alorma.caducity.ui.components.bottomsheet

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.RescheduleItemUseCase
import com.alorma.caducity.domain.usecase.SplitAndConsumeItemUseCase
import com.alorma.caducity.domain.usecase.SplitAndDeleteItemUseCase
import com.alorma.caducity.domain.usecase.SplitAndFreezeItemUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.feature.review.InAppReviewManager
import com.alorma.caducity.feature.review.ShowAppReviewFlag
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.ItemConsumedAction
import com.alorma.caducity.feature.tracking.ItemDeletedAction
import com.alorma.caducity.feature.tracking.ItemFrozenAction
import com.alorma.caducity.feature.tracking.ItemRescheduledAction
import com.alorma.caducity.feature.tracking.ItemUnfrozenAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlin.time.Instant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class ItemActionsViewModel(
  private val categoryId: String,
  private val item: ItemDetailUiModel,
  private val expirationThresholds: ExpirationThresholds,
  private val appClock: AppClock,
  private val consumeItemUseCase: ConsumeItemUseCase,
  private val freezeItemUseCase: FreezeItemUseCase,
  private val unfreezeItemUseCase: UnfreezeItemUseCase,
  private val rescheduleItemUseCase: RescheduleItemUseCase,
  private val deleteItemUseCase: DeleteItemUseCase,
  private val splitAndConsumeItemUseCase: SplitAndConsumeItemUseCase,
  private val splitAndFreezeItemUseCase: SplitAndFreezeItemUseCase,
  private val splitAndDeleteItemUseCase: SplitAndDeleteItemUseCase,
  private val eventTracker: EventTracker,
  private val inAppReviewManager: InAppReviewManager,
  private val showAppReviewFlag: ShowAppReviewFlag,
) : BaseViewModel<Unit, Unit, ItemActionSideEffect>() {
  private val _state = MutableStateFlow(calculateState())
  val state: StateFlow<ItemActionsState> = _state

  private fun ItemStatus.toTrackingString(): String =
    when (this) {
      ItemStatus.Fresh -> "fresh"
      ItemStatus.ExpiringSoon -> "expiring_soon"
      ItemStatus.Expired -> "expired"
      ItemStatus.Frozen -> "frozen"
      ItemStatus.Consumed -> "consumed"
    }

  fun calculateStatusForDate(dateMillis: Long?): AppFeedbackType {
    if (dateMillis == null) return AppFeedbackType.Status(item.status)

    val selectedDate = Instant.fromEpochMilliseconds(dateMillis)
    return ItemStatus
      .calculateStatus(
        expirationDate = selectedDate,
        now = appClock.now(),
        soonExpiringThreshold = expirationThresholds.soonExpiringThreshold,
      ).let {
        AppFeedbackType.Status(it)
      }
  }

  private fun calculateState(): ItemActionsState {
    val actions =
      when (item.status) {
        ItemStatus.Fresh, ItemStatus.ExpiringSoon -> {
          // Fresh and expiring soon items can be consumed, frozen, rescheduled, or deleted
          persistentListOf(
            ItemAction.Consume,
            ItemAction.Freeze,
            ItemAction.Reschedule,
            ItemAction.Delete,
          )
        }

        ItemStatus.Frozen -> {
          // Frozen items can be consumed, unfrozen, rescheduled, or deleted
          persistentListOf(
            ItemAction.Consume,
            ItemAction.Unfreeze,
            ItemAction.Reschedule,
            ItemAction.Delete,
          )
        }

        ItemStatus.Expired -> {
          // Check if item is within consume threshold
          val today = appClock.now().date()
          val expirationDate = item.expirationDate
          val daysSinceExpiration = (today.toEpochDays() - expirationDate.toEpochDays()).toInt()

          if (daysSinceExpiration <= expirationThresholds.consumeExpiredThreshold.inWholeDays) {
            // Within threshold: Show consume with warning, reschedule, and delete
            persistentListOf(
              ItemAction.ConsumeWithWarning,
              ItemAction.Reschedule,
              ItemAction.Delete,
            )
          } else {
            // Beyond threshold: Show reschedule and delete
            persistentListOf(
              ItemAction.Reschedule,
              ItemAction.Delete,
            )
          }
        }

        ItemStatus.Consumed -> {
          // Consumed items show placeholder (for future features)
          persistentListOf(
            ItemAction.Placeholder,
          )
        }
      }

    return ItemActionsState(actions = actions)
  }

  fun onActionClick(action: ItemAction) {
    viewModelScope.launch {
      when (action) {
        ItemAction.Consume -> {
          // Check if item is a pack
          if (item.packSize != null && item.packSize > 1) {
            // Show quantity selector
            emitSideEffect(ItemActionSideEffect.ShowConsumeQuantitySelector(item.packSize))
          } else {
            // Single item: consume directly
            val result = consumeItemUseCase.consumeItem(item.id)
            handleResult(result, action) {
              eventTracker.trackAction(ItemConsumedAction(item.status.toTrackingString()))
            }
          }
        }

        is ItemAction.ConsumeQuantity -> {
          val result =
            splitAndConsumeItemUseCase.splitAndConsume(
              categoryId = categoryId,
              itemId = item.id,
              quantityToConsume = action.quantity,
              forceConsume = false,
            )
          handleResult(result, action) {
            eventTracker.trackAction(
              ItemConsumedAction(
                itemStatus = item.status.toTrackingString(),
                parameters =
                  mapOf(
                    "action_type" to "partial",
                    "quantity" to action.quantity.toString(),
                    "pack_size" to (item.packSize?.toString() ?: "null"),
                  ),
              ),
            )
          }
        }

        ItemAction.ConsumeWithWarning -> {
          // Check if item is a pack
          if (item.packSize != null && item.packSize > 1) {
            // Show quantity selector for expired pack
            emitSideEffect(ItemActionSideEffect.ShowConsumeExpiredQuantitySelector(item.packSize))
          } else {
            // Emit side effect to show warning dialog
            emitSideEffect(ItemActionSideEffect.ShowConsumeExpiredWarning)
          }
        }

        is ItemAction.ConsumeWithWarningQuantity -> {
          val result =
            splitAndConsumeItemUseCase.splitAndConsume(
              categoryId = categoryId,
              itemId = item.id,
              quantityToConsume = action.quantity,
              forceConsume = true,
            )
          handleResult(result, action) {
            eventTracker.trackAction(
              ItemConsumedAction(
                itemStatus = item.status.toTrackingString(),
                parameters =
                  mapOf(
                    "action_type" to "partial",
                    "quantity" to action.quantity.toString(),
                    "pack_size" to (item.packSize?.toString() ?: "null"),
                    "forced" to "true",
                  ),
              ),
            )
          }
        }

        ItemAction.Freeze -> {
          // Check if item is a pack
          if (item.packSize != null && item.packSize > 1) {
            // Show quantity selector
            emitSideEffect(ItemActionSideEffect.ShowFreezeQuantitySelector(item.packSize))
          } else {
            // Single item: freeze directly
            val expirationInstant = item.expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault())
            val result = freezeItemUseCase.freezeItem(item.id, expirationInstant)
            handleResult(result, action) {
              eventTracker.trackAction(ItemFrozenAction(item.status.toTrackingString()))
            }
          }
        }

        is ItemAction.FreezeQuantity -> {
          val result =
            splitAndFreezeItemUseCase.splitAndFreeze(
              categoryId = categoryId,
              itemId = item.id,
              quantityToFreeze = action.quantity,
            )
          handleResult(result, action) {
            eventTracker.trackAction(
              ItemFrozenAction(
                itemStatus = item.status.toTrackingString(),
                parameters =
                  mapOf(
                    "action_type" to "partial",
                    "quantity" to action.quantity.toString(),
                    "pack_size" to (item.packSize?.toString() ?: "null"),
                  ),
              ),
            )
          }
        }

        ItemAction.Unfreeze -> {
          // Check if item is a pack
          if (item.packSize != null && item.packSize > 1) {
            // Show quantity selector
            emitSideEffect(ItemActionSideEffect.ShowUnfreezeQuantitySelector(item.packSize))
          } else {
            // Single item: unfreeze directly
            val result = unfreezeItemUseCase.unfreezeItem(item.id)
            handleResult(result, action) {
              eventTracker.trackAction(ItemUnfrozenAction())
            }
          }
        }

        is ItemAction.UnfreezeQuantity -> {
          val result =
            splitAndFreezeItemUseCase.splitAndUnfreeze(
              categoryId = categoryId,
              itemId = item.id,
              quantityToUnfreeze = action.quantity,
            )
          handleResult(result, action) {
            eventTracker.trackAction(
              ItemUnfrozenAction(
                parameters =
                  mapOf(
                    "action_type" to "partial",
                    "quantity" to action.quantity.toString(),
                    "pack_size" to (item.packSize?.toString() ?: "null"),
                  ),
              ),
            )
          }
        }

        ItemAction.Reschedule -> {
          // Emit side effect to show date picker
          val currentExpirationMillis =
            item.expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
          emitSideEffect(ItemActionSideEffect.ShowRescheduleDatePicker(currentExpirationMillis))
        }

        ItemAction.Delete -> {
          // Check if item is a pack
          if (item.packSize != null && item.packSize > 1) {
            // Show quantity selector
            emitSideEffect(ItemActionSideEffect.ShowDeleteQuantitySelector(item.packSize))
          } else {
            // Single item: delete directly
            val result = deleteItemUseCase.deleteItem(item.id)
            handleResult(result, action) {
              eventTracker.trackAction(ItemDeletedAction(item.status.toTrackingString()))
            }
          }
        }

        is ItemAction.DeleteQuantity -> {
          val result =
            splitAndDeleteItemUseCase.splitAndDelete(
              itemId = item.id,
              quantityToDelete = action.quantity,
            )
          handleResult(result, action) {
            eventTracker.trackAction(
              ItemDeletedAction(
                itemStatus = item.status.toTrackingString(),
                parameters =
                  mapOf(
                    "action_type" to "partial",
                    "quantity" to action.quantity.toString(),
                    "pack_size" to (item.packSize?.toString() ?: "null"),
                  ),
              ),
            )
          }
        }

        ItemAction.Placeholder -> {
          // No action for placeholder
        }
      }
    }
  }

  fun onConfirmConsumeExpired() {
    viewModelScope.launch {
      val result = consumeItemUseCase.forceConsumeItem(item.id)
      handleResult(result, ItemAction.ConsumeWithWarning) {
        eventTracker.trackAction(ItemConsumedAction(item.status.toTrackingString()))
      }
    }
  }

  fun onConfirmReschedule(newDate: LocalDate) {
    viewModelScope.launch {
      if (newDate != item.expirationDate) {
        val daysChanged =
          when {
            newDate < item.expirationDate -> "earlier"
            newDate > item.expirationDate -> "later"
            else -> "no_change"
          }

        val instant = newDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val result = rescheduleItemUseCase.rescheduleItem(item.id, instant)
        handleResult(result, ItemAction.Reschedule) {
          eventTracker.trackAction(
            ItemRescheduledAction(
              itemStatus = item.status.toTrackingString(),
              daysChanged = daysChanged,
            ),
          )
        }
      }
    }
  }

  private fun handleResult(
    result: Result<Unit>,
    action: ItemAction,
    onSuccess: () -> Unit = {},
  ) {
    result
      .onSuccess {
        onSuccess()
        emitSideEffect(ItemActionSideEffect.ActionCompleted(action))
        // Request in-app review after successful action, but only after 3 actions (when counter reaches 0)
        if (showAppReviewFlag.isEnabled()) {
          emitSideEffect(ItemActionSideEffect.RequestInAppReview)
        }
      }.onFailure { error ->
        emitSideEffect(ItemActionSideEffect.ActionFailed(action, error.message))
      }
  }

  override fun navigate(navigation: Unit) {
    // Empty - this ViewModel doesn't navigate
  }
}

sealed interface ItemActionSideEffect {
  data class ActionCompleted(
    val action: ItemAction,
  ) : ItemActionSideEffect

  data class ActionFailed(
    val action: ItemAction,
    val message: String?,
  ) : ItemActionSideEffect

  data object ShowConsumeExpiredWarning : ItemActionSideEffect

  data class ShowConsumeQuantitySelector(
    val maxQuantity: Int,
  ) : ItemActionSideEffect

  data class ShowConsumeExpiredQuantitySelector(
    val maxQuantity: Int,
  ) : ItemActionSideEffect

  data class ShowFreezeQuantitySelector(
    val maxQuantity: Int,
  ) : ItemActionSideEffect

  data class ShowUnfreezeQuantitySelector(
    val maxQuantity: Int,
  ) : ItemActionSideEffect

  data class ShowDeleteQuantitySelector(
    val maxQuantity: Int,
  ) : ItemActionSideEffect

  data class ShowRescheduleDatePicker(
    val currentExpirationMillis: Long,
  ) : ItemActionSideEffect

  data object RequestInAppReview : ItemActionSideEffect
}
