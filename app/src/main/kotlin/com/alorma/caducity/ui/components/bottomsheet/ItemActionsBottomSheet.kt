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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.ThermometerSnow
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Shows a bottom sheet with actions for a specific item.
 * Actions: Consume, Freeze, Delete.
 *
 * @param item The item to show actions for
 * @param itemDisplayText Optional custom text to display in header (defaults to item.identifier)
 */
fun AppBottomSheetState.showItemActionsBottomSheet(
  coroutineScope: CoroutineScope,
  item: Item,
  itemDisplayText: String? = null,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDelete: () -> Unit,
) {
  show(
    appFeedbackType = AppFeedbackType.Status(item.status),
  ) {
    ItemActionsBottomSheetContent(
      itemText = itemDisplayText ?: item.identifier,
      item = item,
      onConsume = {
        onConsume()
        coroutineScope.launch { this@showItemActionsBottomSheet.hide() }
      },
      onFreeze = {
        onFreeze()
        coroutineScope.launch { this@showItemActionsBottomSheet.hide() }
      },
      onDelete = {
        onDelete()
        coroutineScope.launch { this@showItemActionsBottomSheet.hide() }
      },
    )
  }
}

@Composable
private fun ItemActionsBottomSheetContent(
  itemText: String,
  item: Item,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDelete: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 24.dp),
  ) {
    // Header with item info
    Text(
      text = itemText,
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
    )

    HorizontalDivider()

    // Consume action
    ListItem(
      headlineContent = { Text(stringResource(R.string.category_detail_action_consume)) },
      leadingContent = {
        Icon(
          imageVector = AppIcons.Cooking,
          contentDescription = null,
        )
      },
      modifier = Modifier.clickable { onConsume() },
    )

    // Freeze/Unfreeze action
    val freezeText = if (item.status == ItemStatus.Frozen) {
      stringResource(R.string.category_detail_action_unfreeze)
    } else {
      stringResource(R.string.category_detail_action_freeze)
    }

    ListItem(
      headlineContent = { Text(freezeText) },
      leadingContent = {
        Icon(
          imageVector = AppIcons.ThermometerSnow,
          contentDescription = null,
        )
      },
      modifier = Modifier.clickable { onFreeze() },
    )

    // Delete action
    ListItem(
      headlineContent = { Text(stringResource(R.string.category_detail_action_delete)) },
      leadingContent = {
        Icon(
          imageVector = AppIcons.Delete,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
        )
      },
      modifier = Modifier.clickable { onDelete() },
    )
  }
}
