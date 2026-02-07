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
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Shows a bottom sheet with actions for a specific item.
 * Actions are conditional based on item status and expiration thresholds.
 *
 * @param item The item to show actions for (ItemDetailUiModel version)
 * @param onConsume Callback when Consume action is clicked
 * @param onFreeze Callback when Freeze action is clicked
 * @param onUnfreeze Callback when Unfreeze action is clicked
 * @param onDelete Callback when Delete action is clicked
 */
fun AppBottomSheetState.showItemActionsBottomSheet(
  coroutineScope: CoroutineScope,
  item: ItemDetailUiModel,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onUnfreeze: () -> Unit,
  onDelete: () -> Unit,
) {
  show(
    appFeedbackType = AppFeedbackType.Status(item.status),
  ) {
    ItemActionsBottomSheetContent(
      item = item,
      coroutineScope = coroutineScope,
      bottomSheetState = this@showItemActionsBottomSheet,
      onConsume = onConsume,
      onFreeze = onFreeze,
      onUnfreeze = onUnfreeze,
      onDelete = onDelete,
    )
  }
}

/**
 * Legacy overload for Item domain model compatibility.
 * Converts Item to ItemDetailUiModel and delegates to the main function.
 *
 * @deprecated Use the ItemDetailUiModel version instead
 */
@Deprecated("Use ItemDetailUiModel version", ReplaceWith("showItemActionsBottomSheet with ItemDetailUiModel"))
fun AppBottomSheetState.showItemActionsBottomSheet(
  coroutineScope: CoroutineScope,
  item: Item,
  itemDisplayText: String? = null,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDelete: () -> Unit,
) {
  // Convert Item to ItemDetailUiModel
  val itemUiModel = ItemDetailUiModel(
    id = item.id,
    expirationDate = kotlinx.datetime.LocalDate(
      year = 2024,
      monthNumber = 1,
      dayOfMonth = 1
    ), // Placeholder, will be recalculated by ViewModel
    status = item.status,
    text = itemDisplayText ?: item.identifier,
  )

  show(
    appFeedbackType = AppFeedbackType.Status(item.status),
  ) {
    ItemActionsBottomSheetContent(
      item = itemUiModel,
      coroutineScope = coroutineScope,
      bottomSheetState = this@showItemActionsBottomSheet,
      onConsume = onConsume,
      onFreeze = onFreeze,
      onUnfreeze = onFreeze, // Legacy: Freeze and unfreeze use same callback
      onDelete = onDelete,
    )
  }
}

@Composable
private fun ItemActionsBottomSheetContent(
  item: ItemDetailUiModel,
  coroutineScope: CoroutineScope,
  bottomSheetState: AppBottomSheetState,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onUnfreeze: () -> Unit,
  onDelete: () -> Unit,
  viewModel: ItemActionsViewModel = koinViewModel(
    key = "item_actions_${item.id}_${item.status}",
  ) { parametersOf(item) }
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

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
            onClick = {
              onConsume()
              coroutineScope.launch { bottomSheetState.hide() }
            }
          )
        }

        ItemAction.ConsumeWithWarning -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_consume),
            icon = AppIcons.Cooking,
            onClick = {
              onConsume()
              coroutineScope.launch { bottomSheetState.hide() }
            }
          )
        }

        ItemAction.Freeze -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_freeze),
            icon = AppIcons.ThermometerSnow,
            onClick = {
              onFreeze()
              coroutineScope.launch { bottomSheetState.hide() }
            }
          )
        }

        ItemAction.Unfreeze -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_unfreeze),
            icon = AppIcons.ThermometerSnow,
            onClick = {
              onUnfreeze()
              coroutineScope.launch { bottomSheetState.hide() }
            }
          )
        }

        ItemAction.Delete -> {
          ActionListItem(
            text = stringResource(R.string.category_detail_action_delete),
            icon = AppIcons.Delete,
            tint = MaterialTheme.colorScheme.error,
            onClick = {
              onDelete()
              coroutineScope.launch { bottomSheetState.hide() }
            }
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
