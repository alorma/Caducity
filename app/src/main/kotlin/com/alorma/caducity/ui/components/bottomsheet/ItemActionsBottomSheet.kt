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
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Shows a bottom sheet with actions for a specific item.
 * Actions are conditional based on item status and expiration thresholds.
 * All action handling is done internally by ItemActionsViewModel.
 *
 * @param item The item to show actions for
 * @param onActionCompleted Callback when an action is successfully completed
 */
fun AppBottomSheetState.showItemActionsBottomSheet(
  coroutineScope: CoroutineScope,
  item: ItemDetailUiModel,
  onActionCompleted: () -> Unit = {},
) {
  show(
    appFeedbackType = AppFeedbackType.Status(item.status),
  ) {
    ItemActionsBottomSheetContent(
      item = item,
      coroutineScope = coroutineScope,
      bottomSheetState = this@showItemActionsBottomSheet,
      onActionCompleted = onActionCompleted,
    )
  }
}

@Composable
private fun ItemActionsBottomSheetContent(
  item: ItemDetailUiModel,
  coroutineScope: CoroutineScope,
  bottomSheetState: AppBottomSheetState,
  onActionCompleted: () -> Unit,
  viewModel: ItemActionsViewModel = koinViewModel(
    key = "item_actions_${item.id}_${item.status}",
  ) { parametersOf(item) }
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  // Handle side effects
  LaunchedEffect(viewModel) {
    viewModel.sideEffect.collect { effect ->
      when (effect) {
        ItemActionSideEffect.ActionCompleted -> {
          bottomSheetState.hide()
          onActionCompleted()
        }
        is ItemActionSideEffect.ActionFailed -> {
          // TODO: Show error feedback (snackbar/toast)
          bottomSheetState.hide()
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
