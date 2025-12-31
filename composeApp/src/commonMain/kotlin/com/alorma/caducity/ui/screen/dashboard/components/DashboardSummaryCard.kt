package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_section_expired
import caducity.composeapp.generated.resources.dashboard_section_expiring_soon
import caducity.composeapp.generated.resources.dashboard_section_fresh
import caducity.composeapp.generated.resources.dashboard_section_frozen
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.screen.dashboard.DashboardSummary
import com.alorma.caducity.base.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.base.main.InstanceStatus
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardSummaryCard(
  summary: DashboardSummary,
  onStatusClick: (InstanceStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .then(modifier),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // First row: Expired, Expiring Soon
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      SummaryStatusCard(
        status = InstanceStatus.Expired,
        count = summary.expired,
        onClick = { onStatusClick(it) },
        modifier = Modifier.weight(1f),
      )
      SummaryStatusCard(
        status = InstanceStatus.ExpiringSoon,
        count = summary.expiringSoon,
        onClick = { onStatusClick(it) },
        modifier = Modifier.weight(1f),
      )
    }

    // Second row: Fresh, Frozen
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      SummaryStatusCard(
        status = InstanceStatus.Fresh,
        count = summary.fresh,
        onClick = { onStatusClick(it) },
        modifier = Modifier.weight(1f),
      )
      SummaryStatusCard(
        status = InstanceStatus.Frozen,
        count = summary.frozen,
        onClick = { onStatusClick(it) },
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun SummaryStatusCard(
  status: InstanceStatus,
  count: Int,
  onClick: (InstanceStatus) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = ExpirationDefaults.getColors(status)

  val dim = if (count > 0) {
    CaducityTheme.dims.noDim
  } else {
    CaducityTheme.dims.dim3
  }

  Surface(
    modifier = Modifier.then(modifier),
    color = colors.container.copy(alpha = dim),
    contentColor = colors.onContainer,
    shape = MaterialShapes.Square.toShape(),
  ) {
    Column(
      modifier = Modifier
        .clickable(onClick = { onClick(status) })
        .fillMaxWidth()
        .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

      val text = when (status) {
        InstanceStatus.Expired -> stringResource(Res.string.dashboard_section_expired)
        InstanceStatus.ExpiringSoon -> stringResource(Res.string.dashboard_section_expiring_soon)
        InstanceStatus.Fresh -> stringResource(Res.string.dashboard_section_fresh)
        InstanceStatus.Frozen -> stringResource(Res.string.dashboard_section_frozen)
        InstanceStatus.Consumed -> "Consumed" // Consumed items don't appear in dashboard
      }

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Start,
      )

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = count.toString(),
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.End,
      )
    }
  }
}

@Preview
@Composable
private fun DashboardSummaryCardPreview() {
  AppPreview {
    Surface {
      Row {
        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 3,
            expiringSoon = 5,
            fresh = 12,
            frozen = 2,
          ),
          onStatusClick = {},
        )
      }
    }
  }
}