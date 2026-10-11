package com.alorma.caducity.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.status.StatusPill
import com.alorma.caducity.ui.theme.preview.PreviewTheme

// TEMP(fase 7): adaptador sobre StatusPill per a les pantalles velles; s'esborra amb elles.
@Composable
fun StatusBadge(
  status: ItemStatus,
  modifier: Modifier = Modifier,
) {
  val label =
    when (status) {
      ItemStatus.Expired -> stringResource(R.string.expiration_status_badge_expired)
      ItemStatus.ExpiringSoon -> stringResource(R.string.expiration_status_badge_expiring_soon)
      ItemStatus.Fresh -> stringResource(R.string.expiration_status_badge_fresh)
      ItemStatus.Frozen -> stringResource(R.string.expiration_status_badge_frozen)
      ItemStatus.Consumed, ItemStatus.Discarded -> stringResource(R.string.expiration_status_badge_consumed)
    }
  val expiry =
    when (status) {
      ItemStatus.Expired -> ExpiryStatus.EXPIRED
      ItemStatus.ExpiringSoon -> ExpiryStatus.SOON
      ItemStatus.Fresh -> ExpiryStatus.FRESH
      ItemStatus.Frozen -> ExpiryStatus.FROZEN
      ItemStatus.Consumed, ItemStatus.Discarded -> null
    }
  if (expiry != null) {
    StatusPill(status = expiry, label = label, modifier = modifier)
  } else {
    val colors = ExpirationDefaults.getSoftColors(status)
    StatusPill(label = label, container = colors.container, onContainer = colors.onContainer, modifier = modifier)
  }
}

@Preview(name = "Status Badge - All Statuses")
@Composable
internal fun StatusBadgePreview() {
  PreviewTheme {
    Surface {
      Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        listOf(
          ItemStatus.Fresh,
          ItemStatus.ExpiringSoon,
          ItemStatus.Expired,
          ItemStatus.Frozen,
          ItemStatus.Consumed,
        ).forEach {
          StatusBadge(status = it)
        }
      }
    }
  }
}
