package com.alorma.caducity.ui.components.status

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ExpiryStatus

/** «Congelat», «Avui», «10 dies» or «Fa 2 dies»: the label of a unit's [StatusPill]. [daysLeft] is negative once expired. */
@Composable
fun statusPillText(
  status: ExpiryStatus,
  daysLeft: Int,
): String =
  when {
    status == ExpiryStatus.FROZEN -> stringResource(R.string.item_pill_frozen)
    daysLeft == 0 -> stringResource(R.string.item_pill_today)
    daysLeft > 0 -> pluralStringResource(R.plurals.item_pill_days_left, daysLeft, daysLeft)
    else -> pluralStringResource(R.plurals.item_pill_days_ago, -daysLeft, -daysLeft)
  }
