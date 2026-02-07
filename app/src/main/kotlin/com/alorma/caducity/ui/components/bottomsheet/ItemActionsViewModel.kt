package com.alorma.caducity.ui.components.bottomsheet

import androidx.lifecycle.ViewModel
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ItemActionsViewModel(
  private val item: ItemDetailUiModel,
  private val expirationThresholds: ExpirationThresholds,
  private val appClock: AppClock,
) : ViewModel() {

  private val _state = MutableStateFlow(calculateState())
  val state: StateFlow<ItemActionsState> = _state

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

    return ItemActionsState(actions = actions)
  }
}
