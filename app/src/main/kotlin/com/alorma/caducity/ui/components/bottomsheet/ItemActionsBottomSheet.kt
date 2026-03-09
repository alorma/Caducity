package com.alorma.caducity.ui.components.bottomsheet

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.ThermometerSnow
import com.alorma.caducity.base.ui.icons.outlined.Calendar
import com.alorma.caducity.feature.review.InAppReviewManager
import com.alorma.caducity.feature.tracking.ItemActionsBottomSheetScreen
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.LocalAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Shows a bottom sheet with actions for a specific item.
 * Actions are conditional based on item status and expiration thresholds.
 * All action handling is done internally by ItemActionsViewModel.
 *
 * @param categoryId The ID of the category containing the item
 * @param item The item to show actions for
 * @param onActionPerformed Callback for side effects (ActionCompleted, ActionFailed)
 */
fun AppBottomSheetState.showItemActionsBottomSheet(
  coroutineScope: CoroutineScope,
  categoryId: String,
  item: ItemDetailUiModel,
  onActionPerformed: (ItemActionSideEffect) -> Unit,
) {
  show(
    appFeedbackType = AppFeedbackType.Status(item.status),
  ) {
    ItemActionsBottomSheetContent(
      categoryId = categoryId,
      item = item,
      onActionPerformed = { sideEffect ->
        onActionPerformed(sideEffect)
        // Hide bottom sheet after action (except for warning dialog and quantity selectors)
        if (sideEffect !is ItemActionSideEffect.ShowConsumeExpiredWarning &&
          sideEffect !is ItemActionSideEffect.ShowConsumeQuantitySelector &&
          sideEffect !is ItemActionSideEffect.ShowConsumeExpiredQuantitySelector &&
          sideEffect !is ItemActionSideEffect.ShowFreezeQuantitySelector &&
          sideEffect !is ItemActionSideEffect.ShowUnfreezeQuantitySelector &&
          sideEffect !is ItemActionSideEffect.ShowDeleteQuantitySelector
        ) {
          coroutineScope.launch {
            this@showItemActionsBottomSheet.hide()
          }
        }
      },
    )
  }
}

@Composable
private fun ItemActionsBottomSheetContent(
  categoryId: String,
  item: ItemDetailUiModel,
  onActionPerformed: (ItemActionSideEffect) -> Unit,
  viewModel: ItemActionsViewModel =
    koinViewModel(
      key = "item_actions_${item.id}_${item.status}_${item.expirationDate}",
    ) { parametersOf(categoryId, item) },
) {
  TrackScreen(screen = ItemActionsBottomSheetScreen())

  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = LocalAppDialogState.current

  val itemDate =
    item
      .expirationDate
      .atStartOfDayIn(TimeZone.UTC)
      .toEpochMilliseconds()

  val datePickerState: DatePickerState =
    rememberDatePickerState(
      initialSelectedDateMillis = itemDate,
    )

  // Handle side effects
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is ItemActionSideEffect.ActionCompleted,
        is ItemActionSideEffect.ActionFailed,
        ItemActionSideEffect.RequestInAppReview,
        -> {
          // Pass success/error feedback and review request to caller
          onActionPerformed(effect)
        }

        ItemActionSideEffect.ShowConsumeExpiredWarning -> {
          // Handle warning dialog internally
          val result =
            dialogState.showAlertDialog(
              title = { Text(stringResource(R.string.warning_consume_expired_title)) },
              content = { Text(stringResource(R.string.warning_consume_expired_message)) },
              type = AppFeedbackType.Status(item.status),
              positiveButton = { Text(stringResource(R.string.warning_consume_expired_positive)) },
              negativeButton = { Text(stringResource(R.string.warning_consume_expired_negative)) },
            )
          if (result == DialogResult.Positive) {
            viewModel.onConfirmConsumeExpired()
          }
        }

        is ItemActionSideEffect.ShowConsumeQuantitySelector -> {
          val result =
            dialogState.showQuantitySelectorDialog(
              maxQuantity = effect.maxQuantity,
              title = { Text(stringResource(R.string.quantity_selector_consume_title)) },
              positiveButton = { quantity -> Text(stringResource(R.string.quantity_selector_confirm, quantity)) },
              negativeButton = { Text(stringResource(R.string.quantity_selector_cancel)) },
              type = AppFeedbackType.Status(item.status),
            )
          when (result) {
            is QuantitySelectorResult.Selected -> {
              // If user selected max quantity, consume entire pack
              if (result.quantity >= effect.maxQuantity) {
                viewModel.onActionClick(ItemAction.Consume)
              } else {
                viewModel.onActionClick(ItemAction.ConsumeQuantity(result.quantity))
              }
            }

            QuantitySelectorResult.Dismissed -> {
              // User cancelled
            }
          }
        }

        is ItemActionSideEffect.ShowConsumeExpiredQuantitySelector -> {
          val result =
            dialogState.showQuantitySelectorDialog(
              maxQuantity = effect.maxQuantity,
              title = { Text(stringResource(R.string.quantity_selector_consume_expired_title)) },
              positiveButton = { quantity -> Text(stringResource(R.string.quantity_selector_confirm, quantity)) },
              negativeButton = { Text(stringResource(R.string.quantity_selector_cancel)) },
              type = AppFeedbackType.Status(item.status),
            )
          when (result) {
            is QuantitySelectorResult.Selected -> {
              // If user selected max quantity, consume entire pack
              if (result.quantity >= effect.maxQuantity) {
                viewModel.onActionClick(ItemAction.ConsumeWithWarning)
              } else {
                viewModel.onActionClick(ItemAction.ConsumeWithWarningQuantity(result.quantity))
              }
            }

            QuantitySelectorResult.Dismissed -> {
              // User cancelled
            }
          }
        }

        is ItemActionSideEffect.ShowFreezeQuantitySelector -> {
          val result =
            dialogState.showQuantitySelectorDialog(
              maxQuantity = effect.maxQuantity,
              title = { Text(stringResource(R.string.quantity_selector_freeze_title)) },
              positiveButton = { quantity -> Text(stringResource(R.string.quantity_selector_confirm, quantity)) },
              negativeButton = { Text(stringResource(R.string.quantity_selector_cancel)) },
              type = AppFeedbackType.Status(item.status),
            )
          when (result) {
            is QuantitySelectorResult.Selected -> {
              // If user selected max quantity, freeze entire pack
              if (result.quantity >= effect.maxQuantity) {
                viewModel.onActionClick(ItemAction.Freeze)
              } else {
                viewModel.onActionClick(ItemAction.FreezeQuantity(result.quantity))
              }
            }

            QuantitySelectorResult.Dismissed -> {
              // User cancelled
            }
          }
        }

        is ItemActionSideEffect.ShowUnfreezeQuantitySelector -> {
          val result =
            dialogState.showQuantitySelectorDialog(
              maxQuantity = effect.maxQuantity,
              title = { Text(stringResource(R.string.quantity_selector_unfreeze_title)) },
              positiveButton = { quantity -> Text(stringResource(R.string.quantity_selector_confirm, quantity)) },
              negativeButton = { Text(stringResource(R.string.quantity_selector_cancel)) },
              type = AppFeedbackType.Status(item.status),
            )
          when (result) {
            is QuantitySelectorResult.Selected -> {
              // If user selected max quantity, unfreeze entire pack
              if (result.quantity >= effect.maxQuantity) {
                viewModel.onActionClick(ItemAction.Unfreeze)
              } else {
                viewModel.onActionClick(ItemAction.UnfreezeQuantity(result.quantity))
              }
            }

            QuantitySelectorResult.Dismissed -> {
              // User cancelled
            }
          }
        }

        is ItemActionSideEffect.ShowDeleteQuantitySelector -> {
          val result =
            dialogState.showQuantitySelectorDialog(
              maxQuantity = effect.maxQuantity,
              title = { Text(stringResource(R.string.quantity_selector_delete_title)) },
              positiveButton = { quantity -> Text(stringResource(R.string.quantity_selector_confirm, quantity)) },
              negativeButton = { Text(stringResource(R.string.quantity_selector_cancel)) },
              type = AppFeedbackType.Status(item.status),
            )
          when (result) {
            is QuantitySelectorResult.Selected -> {
              // If user selected max quantity, delete entire pack
              if (result.quantity >= effect.maxQuantity) {
                viewModel.onActionClick(ItemAction.Delete)
              } else {
                viewModel.onActionClick(ItemAction.DeleteQuantity(result.quantity))
              }
            }

            QuantitySelectorResult.Dismissed -> {
              // User cancelled
            }
          }
        }

        is ItemActionSideEffect.ShowRescheduleDatePicker -> {
          val result =
            dialogState.showDatePickerDialog(
              datePickerState = datePickerState,
              positiveButton = {
                Text(stringResource(R.string.category_detail_add_item_date_picker_ok))
              },
              negativeButton = {
                Text(stringResource(R.string.category_detail_add_item_date_picker_cancel))
              },
              type = AppFeedbackType.Status(item.status),
              onDateSelected = { selectedDateMillis ->
                viewModel.calculateStatusForDate(selectedDateMillis)
              },
            )

          if (result == DialogResult.Positive) {
            val newDateMillis = datePickerState.selectedDateMillis
            if (newDateMillis != null) {
              viewModel.onConfirmReschedule(
                newDate =
                  Instant
                    .fromEpochMilliseconds(newDateMillis)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date,
              )
            }
          }
        }
      }
    }
  }

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp),
  ) {
    // Header with item info and pack badge
    Row(
      modifier =
        Modifier
          .padding(horizontal = 24.dp, vertical = 16.dp)
          .clip(CaducityTheme.shapes.small),
      horizontalArrangement = Arrangement.spacedBy(0.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .background(CaducityTheme.colorScheme.outline)
            .padding(8.dp),
      ) {
        if (item.packSize != null && item.packSize > 1) {
          Text(
            text = item.packSize.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.surface,
          )
        }
      }

      Text(
        text = item.text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = if (item.packSize != null && item.packSize > 1) 8.dp else 0.dp),
      )
    }

    HorizontalDivider()

    // Render actions based on state
    state.actions.forEach { action ->
      when (action) {
        ItemAction.Consume -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_consume),
            icon = AppIcons.Cooking,
            onClick = { viewModel.onActionClick(action) },
          )
        }

        ItemAction.ConsumeWithWarning -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_consume),
            icon = AppIcons.Cooking,
            onClick = { viewModel.onActionClick(action) },
          )
        }

        ItemAction.Freeze -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_freeze),
            icon = AppIcons.ThermometerSnow,
            onClick = { viewModel.onActionClick(action) },
          )
        }

        ItemAction.Unfreeze -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_unfreeze),
            icon = AppIcons.ThermometerSnow,
            onClick = { viewModel.onActionClick(action) },
          )
        }

        ItemAction.Reschedule -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_reschedule),
            icon = AppIcons.Outlined.Calendar,
            onClick = { viewModel.onActionClick(action) },
          )
        }

        ItemAction.Delete -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_delete),
            icon = AppIcons.Delete,
            tint = MaterialTheme.colorScheme.error,
            onClick = { viewModel.onActionClick(action) },
          )
        }

        ItemAction.Placeholder -> {
          // Placeholder for consumed items (future feature)
          Text(
            text = stringResource(R.string.category_detail_consumed_item_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
          )
        }

        // Quantity-based actions are never shown in the bottom sheet list
        // They are only triggered via the quantity selector dialog
        is ItemAction.ConsumeQuantity,
        is ItemAction.ConsumeWithWarningQuantity,
        is ItemAction.FreezeQuantity,
        is ItemAction.UnfreezeQuantity,
        is ItemAction.DeleteQuantity,
        -> {
          // These actions are not displayed in the bottom sheet
          // They are handled internally after quantity selection
        }
      }
    }
  }
}

@Composable
private fun ActionListItem(
  text: String,
  icon: ImageVector,
  onClick: () -> Unit,
  tint: Color = MaterialTheme.colorScheme.onSurface,
) {
  ListItem(
    headlineContent = { Text(text) },
    leadingContent = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
      )
    },
    modifier = Modifier.clickable { onClick() },
  )
}

/**
 * Handles item action side effects by showing appropriate snackbar feedback.
 * Use this in side effect handlers where item actions are performed.
 *
 * @param sideEffect The side effect to handle
 * @param snackbarState The snackbar state to show feedback
 * @param activity The activity context (optional, needed for in-app review)
 * @param inAppReviewManager The in-app review manager (optional, needed for in-app review)
 * @return The resource ID for the message, or null if no message should be shown
 */
suspend fun handleItemActionSideEffect(
  sideEffect: ItemActionSideEffect,
  snackbarState: AppSnackbarState,
  activity: Activity? = null,
  inAppReviewManager: InAppReviewManager? = null,
) {
  when (sideEffect) {
    is ItemActionSideEffect.ActionCompleted -> {
      val messageRes =
        when (sideEffect.action) {
          ItemAction.Consume,
          is ItemAction.ConsumeQuantity,
          ItemAction.ConsumeWithWarning,
          is ItemAction.ConsumeWithWarningQuantity,
          -> R.string.success_item_consumed

          ItemAction.Freeze,
          is ItemAction.FreezeQuantity,
          -> R.string.success_item_frozen

          ItemAction.Unfreeze,
          is ItemAction.UnfreezeQuantity,
          -> R.string.success_item_unfrozen

          ItemAction.Reschedule -> R.string.success_item_rescheduled
          ItemAction.Delete,
          is ItemAction.DeleteQuantity,
          -> R.string.success_item_deleted

          ItemAction.Placeholder -> null
        }
      messageRes?.let {
        snackbarState.showSnackbar(
          message = it,
          type = AppFeedbackType.Success,
        )
      }
    }

    is ItemActionSideEffect.ActionFailed -> {
      val messageRes =
        when (sideEffect.action) {
          ItemAction.Consume,
          is ItemAction.ConsumeQuantity,
          ItemAction.ConsumeWithWarning,
          is ItemAction.ConsumeWithWarningQuantity,
          -> R.string.error_consume_item_failed

          ItemAction.Freeze,
          is ItemAction.FreezeQuantity,
          -> R.string.error_freeze_item_failed

          ItemAction.Unfreeze,
          is ItemAction.UnfreezeQuantity,
          -> R.string.error_unfreeze_item_failed

          ItemAction.Reschedule -> R.string.error_reschedule_item_failed
          ItemAction.Delete,
          is ItemAction.DeleteQuantity,
          -> R.string.error_delete_item_failed

          ItemAction.Placeholder -> null
        }
      messageRes?.let {
        snackbarState.showSnackbar(
          message = it,
          type = AppFeedbackType.Error,
        )
      }
    }

    ItemActionSideEffect.ShowConsumeExpiredWarning -> {
      // Warning dialog is handled internally by the bottom sheet
    }

    is ItemActionSideEffect.ShowConsumeQuantitySelector,
    is ItemActionSideEffect.ShowConsumeExpiredQuantitySelector,
    is ItemActionSideEffect.ShowFreezeQuantitySelector,
    is ItemActionSideEffect.ShowUnfreezeQuantitySelector,
    is ItemActionSideEffect.ShowDeleteQuantitySelector,
    is ItemActionSideEffect.ShowRescheduleDatePicker,
    -> {
      // Dialogs are handled internally by the bottom sheet
    }

    ItemActionSideEffect.RequestInAppReview -> {
      // Request in-app review if activity and manager are available
      if (activity != null && inAppReviewManager != null) {
        inAppReviewManager.requestReview(activity)
      }
    }
  }
}
