package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.screen.dashboard.DashboardSummary
import com.alorma.caducity.ui.screen.dashboard.ExpirationDefaults
import com.alorma.caducity.domain.model.InstanceStatus

@Composable
fun DashboardSummaryCard(
  summary: DashboardSummary,
  onStatusClick: (InstanceStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .then(modifier),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Spacer(modifier = Modifier.width(16.dp))
    SummaryStatusCard(
      status = InstanceStatus.Expired,
      count = summary.expired,
      onClick = { onStatusClick(it) },
    )
    SummaryStatusCard(
      status = InstanceStatus.ExpiringSoon,
      count = summary.expiringSoon,
      onClick = { onStatusClick(it) },
    )
    SummaryStatusCard(
      status = InstanceStatus.Fresh,
      count = summary.fresh,
      onClick = { onStatusClick(it) },
    )
    Spacer(modifier = Modifier.width(16.dp))
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
        .padding(24.dp)
        .width(120.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = ExpirationDefaults.getText(status),
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
          ),
          onStatusClick = {},
        )
      }
    }
  }
}