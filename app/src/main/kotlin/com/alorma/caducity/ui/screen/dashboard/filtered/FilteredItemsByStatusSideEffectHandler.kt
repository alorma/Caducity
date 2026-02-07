package com.alorma.caducity.ui.screen.dashboard.filtered

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.bottomsheet.showItemActionsBottomSheet
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState

@Composable
fun SideEffectHandler(
  viewModel: FilteredItemsByStatusViewModel,
  bottomSheetState: AppBottomSheetState,
  snackbarState: AppSnackbarState,
) {
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is FilteredItemsByStatusSideEffect.ShowProductItemsBottomSheet -> {
          bottomSheetState.showProductItemsBottomSheet(
            coroutineScope = this,
            productName = effect.productName,
            items = effect.items,
            onItemClick = { item ->
              viewModel.onItemClick(item)
            },
          )
        }

        is FilteredItemsByStatusSideEffect.ShowItemActionsBottomSheet -> {
          bottomSheetState.showItemActionsBottomSheet(
            coroutineScope = this,
            item = effect.item,
            onConsume = {
              viewModel.onConsumeItem(effect.item)
            },
            onFreeze = {
              viewModel.onFreezeItem(effect.item)
            },
            onDelete = {
              viewModel.onDeleteItem(effect.item)
            },
          )
        }

        FilteredItemsByStatusSideEffect.ItemConsumed -> {
          snackbarState.showSnackbar(
            message = R.string.success_item_consumed,
            type = AppFeedbackType.Success,
          )
        }

        FilteredItemsByStatusSideEffect.ItemFrozen -> {
          snackbarState.showSnackbar(
            message = R.string.success_item_frozen,
            type = AppFeedbackType.Success,
          )
        }

        FilteredItemsByStatusSideEffect.ItemUnfrozen -> {
          snackbarState.showSnackbar(
            message = R.string.success_item_unfrozen,
            type = AppFeedbackType.Success,
          )
        }

        FilteredItemsByStatusSideEffect.ItemDeleted -> {
          snackbarState.showSnackbar(
            message = R.string.success_item_deleted,
            type = AppFeedbackType.Success,
          )
        }

        is FilteredItemsByStatusSideEffect.ItemActionFailed -> {
          snackbarState.showSnackbar(
            message = effect.message,
            type = AppFeedbackType.Error,
          )
        }
      }
    }
  }
}
