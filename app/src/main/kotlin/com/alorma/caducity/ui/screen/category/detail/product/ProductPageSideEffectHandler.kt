package com.alorma.caducity.ui.screen.category.detail.product

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.feedback.AppFeedbackResource
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import kotlinx.coroutines.launch

@Composable
internal fun ProductPageSideEffectHandler(
  viewModel: ProductPageViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  bottomSheetState: AppBottomSheetState,
) {
  LaunchedEffect(viewModel.sideEffect) {
    viewModel.sideEffect.collect { effect ->
      when (effect) {
        ProductPageSideEffect.ItemConsumed -> launch {
          snackbarState.showSnackbar(
            message = R.string.success_item_consumed,
            type = AppFeedbackType.Success,
          )
        }

        ProductPageSideEffect.ItemFrozen -> launch {
          snackbarState.showSnackbar(
            message = R.string.success_item_frozen,
            type = AppFeedbackType.Status(ItemStatus.Frozen),
          )
        }

        ProductPageSideEffect.ItemDeleted -> launch {
          snackbarState.showSnackbar(
            message = R.string.success_item_deleted,
            type = AppFeedbackType.Success,
          )
        }

        ProductPageSideEffect.ConsumeItemFailed -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_consume_item_failed,
            type = AppFeedbackType.Error,
          )
        }

        ProductPageSideEffect.FreezeItemFailed -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_freeze_item_failed,
            type = AppFeedbackType.Error,
          )
        }

        ProductPageSideEffect.DeleteItemFailed -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_delete_item_failed,
            type = AppFeedbackType.Error,
          )
        }

        is ProductPageSideEffect.FreezeNotAvailable -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_cannot_freeze_expired,
            type = AppFeedbackType.Status(effect.status),
          )
        }

        is ProductPageSideEffect.ShowConsumeExpiredWarning -> launch {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_title
            ),
            text = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_message
            ),
            type = AppFeedbackType.Status(effect.item.status),
            positiveButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_positive
            ),
            negativeButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_negative
            ),
          )
          if (result == DialogResult.Positive) {
            viewModel.onConsumeItemConfirmed(effect.item)
          }
        }

        is ProductPageSideEffect.ShowConsumeExpiredError -> launch {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.error_cannot_consume_expired
            ),
            text = AppFeedbackResource.AsResource(
              R.string.error_cannot_consume_expired_message
            ),
            type = AppFeedbackType.Status(effect.status),
            positiveButton = AppFeedbackResource.AsResource(
              R.string.error_cannot_consume_expired_positive
            ),
            negativeButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_negative
            ),
          )
          if (result == DialogResult.Positive) {
            viewModel.onDeleteItem(effect.item)
          }
        }

        is ProductPageSideEffect.ShowItemActionsBottomSheet -> launch {
          bottomSheetState.showItemActionsBottomSheet(
            coroutineScope = this@LaunchedEffect,
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
      }
    }
  }
}
