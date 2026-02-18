package com.alorma.caducity.ui.screen.category.detail.product

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ProductDeletionStrategy
import com.alorma.caducity.feature.review.InAppReviewManager
import com.alorma.caducity.ui.components.bottomsheet.handleItemActionSideEffect
import com.alorma.caducity.ui.components.bottomsheet.showItemActionsBottomSheet
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.utils.findActivity
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
internal fun ProductPageSideEffectHandler(
  viewModel: ProductPageViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  bottomSheetState: AppBottomSheetState,
  onNavigateToAddItem: (categoryId: String, productId: String?) -> Unit,
) {
  val context = LocalContext.current
  val activity = context.findActivity()
  val inAppReviewManager: InAppReviewManager = koinInject()
  
  // Collect navigation side effects
  LaunchedEffect(viewModel.navigationSideEffects) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        is ProductPageNavigationSideEffect.NavigateToAddItem -> {
          onNavigateToAddItem(effect.categoryId, effect.productId)
        }
      }
    }
  }

  // Collect other side effects
  LaunchedEffect(viewModel.sideEffects) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        ProductPageSideEffect.ItemDeleted -> launch {
          snackbarState.showSnackbar(
            message = R.string.success_item_deleted,
            type = AppFeedbackType.Success,
          )
        }

        ProductPageSideEffect.DeleteItemFailed -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_delete_item_failed,
            type = AppFeedbackType.Error,
          )
        }

        is ProductPageSideEffect.ShowItemActionsBottomSheet -> {
          bottomSheetState.showItemActionsBottomSheet(
            coroutineScope = this,
            item = effect.item,
            onActionPerformed = { actionSideEffect ->
              launch {
                handleItemActionSideEffect(
                  sideEffect = actionSideEffect,
                  snackbarState = snackbarState,
                  activity = activity,
                  inAppReviewManager = inAppReviewManager,
                )
              }
            },
          )
        }

        // Product-level success events
        ProductPageSideEffect.ProductDeleted -> launch {
          bottomSheetState.hide()
          snackbarState.showSnackbar(
            message = R.string.success_product_deleted,
            type = AppFeedbackType.Success,
          )
        }

        ProductPageSideEffect.ItemsCleared -> launch {
          bottomSheetState.hide()
          snackbarState.showSnackbar(
            message = R.string.success_items_cleared,
            type = AppFeedbackType.Success,
          )
        }

        // Product-level error events
        ProductPageSideEffect.DeleteProductFailed -> launch {
          bottomSheetState.hide()
          snackbarState.showSnackbar(
            message = R.string.error_delete_product_failed,
            type = AppFeedbackType.Error,
          )
        }

        ProductPageSideEffect.ClearItemsFailed -> launch {
          bottomSheetState.hide()
          snackbarState.showSnackbar(
            message = R.string.error_clear_items_failed,
            type = AppFeedbackType.Error,
          )
        }

        // Product-level dialog events
        is ProductPageSideEffect.ShowDeleteProductDialog -> launch {
          val result = dialogState.showAlertDialog(
            title = { Text(stringResource(R.string.product_delete_dialog_title)) },
            content = { Text(stringResource(R.string.product_delete_dialog_message)) },
            type = AppFeedbackType.Info,
            positiveButton = { Text(stringResource(R.string.product_delete_dialog_delete)) },
            negativeButton = { Text(stringResource(R.string.product_delete_dialog_cancel)) },
          )
          if (result == DialogResult.Positive) {
            effect.onDeleteProduct(
              effect.productId,
              ProductDeletionStrategy.CascadeDelete,
            )
          }
        }

        is ProductPageSideEffect.ShowDeleteProductWithItemsDialog -> {
          bottomSheetState.showDeleteProductWithItemsBottomSheet(
            coroutineScope = this,
            itemCount = effect.activeItemCount,
            availableProducts = effect.availableProducts,
            onMoveToStandalone = {
              effect.onDeleteProduct(
                effect.productId,
                ProductDeletionStrategy.MoveToStandalone,
              )
            },
            onMoveToProduct = { targetProductId ->
              effect.onDeleteProduct(
                effect.productId,
                ProductDeletionStrategy.MoveToProduct(targetProductId),
              )
            },
            onCascadeDelete = {
              effect.onDeleteProduct(
                effect.productId,
                ProductDeletionStrategy.CascadeDelete,
              )
            },
          )
        }

        is ProductPageSideEffect.ShowClearProductItemsDialog -> {
          bottomSheetState.showClearItemsBottomSheet(
            coroutineScope = this,
            onClearConsumed = {
              effect.onClearProductItems(effect.productId, false)
            },
            onClearAll = {
              effect.onClearProductItems(effect.productId, true)
            },
          )
        }
      }
    }
  }
}
