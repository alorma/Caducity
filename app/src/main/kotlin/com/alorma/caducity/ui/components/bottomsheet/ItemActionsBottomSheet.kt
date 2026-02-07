package com.alorma.caducity.ui.components.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.ThermometerSnow
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.LocalAppDialogState
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Shows a bottom sheet with actions for a specific item.
 * Actions are conditional based on item status and expiration thresholds.
 * All action handling is done internally by ItemActionsViewModel.
 *
 * @param item The item to show actions for
 * @param onActionPerformed Callback for side effects (ActionCompleted, ActionFailed)
 */
fun AppBottomSheetState.showItemActionsBottomSheet(
  coroutineScope: CoroutineScope,
  item: ItemDetailUiModel,
  onActionPerformed: (ItemActionSideEffect) -> Unit,
) {
  show(
    appFeedbackType = AppFeedbackType.Status(item.status),
  ) {
    ItemActionsBottomSheetContent(
      item = item,
      onActionPerformed = { sideEffect ->
        onActionPerformed(sideEffect)
        // Hide bottom sheet after action (except for warning dialog)
        if (sideEffect !is ItemActionSideEffect.ShowConsumeExpiredWarning) {
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
  item: ItemDetailUiModel,
  onActionPerformed: (ItemActionSideEffect) -> Unit,
  viewModel: ItemActionsViewModel = koinViewModel(
    key = "item_actions_${item.id}_${item.status}",
  ) { parametersOf(item) }
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = LocalAppDialogState.current

  // Handle side effects
  LaunchedEffect(viewModel) {
    viewModel.sideEffect.collect { effect ->
      when (effect) {
        is ItemActionSideEffect.ActionCompleted,
        is ItemActionSideEffect.ActionFailed -> {
          // Pass success/error feedback to caller
          onActionPerformed(effect)
        }

        ItemActionSideEffect.ShowConsumeExpiredWarning -> {
          // Handle warning dialog internally
          val result = dialogState.showAlertDialog(
            title = { Text(stringResource(R.string.warning_consume_expired_title)) },
            text = { Text(stringResource(R.string.warning_consume_expired_message)) },
            type = AppFeedbackType.Status(item.status),
            positiveButton = { Text(stringResource(R.string.warning_consume_expired_positive)) },
            negativeButton = { Text(stringResource(R.string.warning_consume_expired_negative)) },
          )
          if (result == DialogResult.Positive) {
            viewModel.onConfirmConsumeExpired()
          }
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 24.dp),
  ) {
    // Header with item info
    Text(
      text = item.text,
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
    )

    HorizontalDivider()

    // Render actions based on state
    state.actions.forEach { action ->
      when (action) {
        ItemAction.Consume -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_consume),
            icon = AppIcons.Cooking,
            onClick = { viewModel.onActionClick(action) }
          )
        }

        ItemAction.ConsumeWithWarning -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_consume),
            icon = AppIcons.Cooking,
            onClick = { viewModel.onActionClick(action) }
          )
        }

        ItemAction.Freeze -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_freeze),
            icon = AppIcons.ThermometerSnow,
            onClick = { viewModel.onActionClick(action) }
          )
        }

        ItemAction.Unfreeze -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_unfreeze),
            icon = AppIcons.ThermometerSnow,
            onClick = { viewModel.onActionClick(action) }
          )
        }

        ItemAction.Delete -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_delete),
            icon = AppIcons.Delete,
            tint = MaterialTheme.colorScheme.error,
            onClick = { viewModel.onActionClick(action) }
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
      }
    }
  }
}

@Composable
private fun ActionListItem(
  text: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
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
 * @return The resource ID for the message, or null if no message should be shown
 */
suspend fun handleItemActionSideEffect(
  sideEffect: ItemActionSideEffect,
  snackbarState: com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState,
) {
  when (sideEffect) {
    is ItemActionSideEffect.ActionCompleted -> {
      val messageRes = when (sideEffect.action) {
        ItemAction.Consume, ItemAction.ConsumeWithWarning -> R.string.success_item_consumed
        ItemAction.Freeze -> R.string.success_item_frozen
        ItemAction.Unfreeze -> R.string.success_item_unfrozen
        ItemAction.Delete -> R.string.success_item_deleted
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
      val messageRes = when (sideEffect.action) {
        ItemAction.Consume, ItemAction.ConsumeWithWarning -> R.string.error_consume_item_failed
        ItemAction.Freeze -> R.string.error_freeze_item_failed
        ItemAction.Unfreeze -> R.string.error_unfreeze_item_failed
        ItemAction.Delete -> R.string.error_delete_item_failed
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
  }
}
