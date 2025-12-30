package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_filter_expired
import caducity.composeapp.generated.resources.dashboard_filter_expiring_soon
import caducity.composeapp.generated.resources.dashboard_filter_fresh
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.screen.dashboard.DashboardSummary
import com.alorma.caducity.ui.screen.dashboard.ExpirationColors
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardSummaryCard(
  summary: DashboardSummary,
  modifier: Modifier = Modifier,
) {
  HorizontalUncontainedCarousel(
    modifier = modifier,
    state = rememberCarouselState { 3 },
    itemWidth = 140.dp,
    itemSpacing = 8.dp,
    contentPadding = PaddingValues(horizontal = 16.dp),
  ) { position ->
    val type = when (position) {
      0 -> InstanceStatus.Expired
      1 -> InstanceStatus.ExpiringSoon
      else -> InstanceStatus.Fresh
    }

    val items = when (position) {
      0 -> summary.expired
      1 -> summary.expiringSoon
      else -> summary.fresh
    }

    val shape = MaterialShapes.Pill.toShape(
      startAngle = 45,
    )

    SummaryStatusCard(
      modifier = Modifier.maskClip(shape),
      status = type,
      shape = shape,
      count = items,
    )
  }
}

@Composable
private fun SummaryStatusCard(
  status: InstanceStatus,
  count: Int,
  shape: Shape,
  modifier: Modifier = Modifier,
) {
  val colors = ExpirationColors.getSectionColors(status)

  Surface(
    modifier = Modifier
      .widthIn(140.dp)
      .then(modifier),
    color = colors.container,
    contentColor = colors.onContainer,
    shape = shape,
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      val label = when (status) {
        InstanceStatus.Expired -> stringResource(Res.string.dashboard_filter_expired)
        InstanceStatus.ExpiringSoon -> stringResource(Res.string.dashboard_filter_expiring_soon)
        InstanceStatus.Fresh -> stringResource(Res.string.dashboard_filter_fresh)
      }

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
      )

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = count.toString(),
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Preview
@Composable
private fun DashboardSummaryCardPreview() {
  AppPreview {
    Surface {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 3,
            expiringSoon = 5,
            fresh = 12,
          )
        )

        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 8,
            expiringSoon = 0,
            fresh = 4,
          )
        )

        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 0,
            expiringSoon = 2,
            fresh = 0,
          )
        )
      }
    }
  }
}