package com.alorma.caducity.ui.components.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.InstanceActionResult
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class ItemActionsViewModel(
  private val item: ItemDetailUiModel,
  private val expirationThresholds: ExpirationThresholds,
  private val appClock: AppClock,
  private val consumeItemUseCase: ConsumeItemUseCase,
  private val freezeItemUseCase: FreezeItemUseCase,
  private val unfreezeItemUseCase: UnfreezeItemUseCase,
  private val deleteItemUseCase: DeleteItemUseCase,
) : ViewModel() {

  private val _state = MutableStateFlow(calculateState())
  val state: StateFlow<ItemActionsState> = _state

  private val _sideEffect = Channel<ItemActionSideEffect>(Channel.BUFFERED)
  val sideEffect = _sideEffect.receiveAsFlow()

  private fun calculateState(): ItemActionsState {
    val actions = when (item.status) {
      ItemStatus.Fresh, ItemStatus.ExpiringSoon -> {
        // Fresh and expiring soon items can be consumed, frozen, or deleted
        persistentListOf(
          ItemAction.Consume,
          ItemAction.Freeze,
          ItemAction.Delete,
        )
      }

      ItemStatus.Frozen -> {
        // Frozen items can be consumed, unfrozen, or deleted
        persistentListOf(
          ItemAction.Consume,
          ItemAction.Unfreeze,
          ItemAction.Delete,
        )
      }

      ItemStatus.Expired -> {
        // Check if item is within consume threshold
        val today = appClock.now().date()
        val expirationDate = item.expirationDate
        val daysSinceExpiration = (today.toEpochDays() - expirationDate.toEpochDays()).toInt()

        if (daysSinceExpiration <= expirationThresholds.consumeExpiredThreshold.inWholeDays) {
          // Within threshold: Show consume with warning + delete
          persistentListOf(
            ItemAction.ConsumeWithWarning,
            ItemAction.Delete,
          )
        } else {
          // Beyond threshold: Show only delete
          persistentListOf(
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

    return ItemActionsState(
      actions = actions,
      showConsumeExpiredWarning = false,
    )
  }

  fun onActionClick(action: ItemAction) {
    viewModelScope.launch {
      when (action) {
        ItemAction.Consume -> {
          val result = consumeItemUseCase.consumeItem(item.id)
          handleResult(result, ItemActionSideEffect.ActionCompleted)
        }

        ItemAction.ConsumeWithWarning -> {
          // Show warning dialog instead of consuming directly
          _state.value = _state.value.copy(showConsumeExpiredWarning = true)
        }

        ItemAction.Freeze -> {
          val expirationInstant = item.expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault())
          val result = freezeItemUseCase.freezeItem(item.id, expirationInstant)
          handleResult(result, ItemActionSideEffect.ActionCompleted)
        }

        ItemAction.Unfreeze -> {
          val result = unfreezeItemUseCase.unfreezeItem(item.id)
          handleResult(result, ItemActionSideEffect.ActionCompleted)
        }

        ItemAction.Delete -> {
          val result = deleteItemUseCase.deleteItem(item.id)
          result.onSuccess {
            emitSideEffect(ItemActionSideEffect.ActionCompleted)
          }.onFailure { error ->
            emitSideEffect(ItemActionSideEffect.ActionFailed(error.message))
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
      _state.value = _state.value.copy(showConsumeExpiredWarning = false)
      val result = consumeItemUseCase.forceConsumeItem(item.id)
      handleResult(result, ItemActionSideEffect.ActionCompleted)
    }
  }

  fun onDismissConsumeExpiredWarning() {
    _state.value = _state.value.copy(showConsumeExpiredWarning = false)
  }

  private fun handleResult(result: InstanceActionResult<Unit>, successEffect: ItemActionSideEffect) {
    when (result) {
      is InstanceActionResult.Success -> emitSideEffect(successEffect)
      is InstanceActionResult.Failure -> emitSideEffect(ItemActionSideEffect.ActionFailed(result.error.toString()))
    }
  }

  private fun emitSideEffect(effect: ItemActionSideEffect) {
    viewModelScope.launch {
      _sideEffect.send(effect)
    }
  }
}

sealed interface ItemActionSideEffect {
  data object ActionCompleted : ItemActionSideEffect
  data class ActionFailed(val message: String?) : ItemActionSideEffect
}
