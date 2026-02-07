package com.alorma.caducity.ui.screen.category.detail.product

import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.ui.components.bottomsheet.showItemActionsBottomSheet
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.coroutines.CoroutineScope

/**
 * Shows a bottom sheet with actions for a specific item (ItemDetailUiModel version).
 * This is a convenience wrapper that converts ItemDetailUiModel to Item domain model.
 * Actions: Consume, Freeze, Delete.
 */
fun AppBottomSheetState.showItemActionsBottomSheet(
  coroutineScope: CoroutineScope,
  item: ItemDetailUiModel,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDelete: () -> Unit,
) {
  // Convert ItemDetailUiModel to Item domain model for the shared bottom sheet
  val domainItem = Item(
    id = item.id,
    identifier = item.text,
    productId = null,
    expirationDate = kotlinx.datetime.Instant.fromEpochMilliseconds(
      item.expirationDate.toEpochDays() * 24 * 60 * 60 * 1000L
    ),
    status = item.status,
    pausedDate = null,
  )

  showItemActionsBottomSheet(
    coroutineScope = coroutineScope,
    item = domainItem,
    itemDisplayText = item.text,
    onConsume = onConsume,
    onFreeze = onFreeze,
    onDelete = onDelete,
  )
}
