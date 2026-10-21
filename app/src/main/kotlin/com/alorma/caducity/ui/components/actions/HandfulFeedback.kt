package com.alorma.caducity.ui.components.actions

import com.alorma.caducity.R
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.SnackbarRequest

/**
 * The ✓ of a handful row (Grapats): lowers the batch a level and says so, «Cacauets: ara en queda poc · Desfés», or
 * «Cacauets consumits · Desfés» from the lowest one. [level] is the one the row shows now.
 */
suspend fun ConsumeItemUseCase.lowerWithSnackbar(
  itemId: String,
  name: String,
  level: HandfulLevel?,
  restoreItems: RestoreItemsUseCase,
): Result<SnackbarRequest> =
  lowerLevel(itemId).map { before ->
    val message =
      when (level?.lower()) {
        HandfulLevel.LOTS -> R.string.handful_snackbar_lots
        HandfulLevel.HALF -> R.string.handful_snackbar_half
        HandfulLevel.LITTLE -> R.string.handful_snackbar_little
        null -> R.string.handful_snackbar_consumed
      }
    SnackbarRequest(
      message = message,
      messageArgs = listOf(name),
      actionLabel = R.string.add_undo,
      type = AppFeedbackType.Success,
      onAction = { restoreItems.restoreSnapshots(listOf(before)) },
    )
  }
