package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.adaptive.rememberIsExpanded
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.screen.dashboard.DashboardSummary
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.LocalThemeTone
import com.alorma.caducity.ui.theme.ThemeTone
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@Composable
fun DashboardSummaryCard(
  summary: DashboardSummary,
  onStatusClick: (ItemStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isExpanded = rememberIsExpanded()

  if (isExpanded) {
    DashboardSummaryCardExpanded(
      summary = summary,
      onStatusClick = onStatusClick,
      modifier = modifier,
    )
  } else {
    DashboardSummaryCardCompact(
      summary = summary,
      onStatusClick = onStatusClick,
      modifier = modifier,
    )
  }
}

@Composable
private fun DashboardSummaryCardCompact(
  summary: DashboardSummary,
  onStatusClick: (ItemStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  val arrangement = Arrangement.spacedBy(8.dp)

  val largeShape = CaducityTheme.shapes.largeIncreased
  val smallShape = CaducityTheme.shapes.small

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    verticalArrangement = arrangement,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = arrangement,
    ) {
      SummaryStatusCard(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(
          topStart = largeShape.topStart,
          topEnd = smallShape.topStart,
          bottomStart = smallShape.topStart,
          bottomEnd = smallShape.topStart,
        ),
        status = ItemStatus.Expired,
        count = summary.expired,
        onClick = { onStatusClick(it) },
      )
      SummaryStatusCard(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(
          topStart = smallShape.topStart,
          topEnd = largeShape.topStart,
          bottomStart = smallShape.topStart,
          bottomEnd = smallShape.topStart,
        ),
        status = ItemStatus.ExpiringSoon,
        count = summary.expiringSoon,
        onClick = { onStatusClick(it) },
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = arrangement,
    ) {
      SummaryStatusCard(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(
          topStart = smallShape.topStart,
          topEnd = smallShape.topStart,
          bottomStart = largeShape.topStart,
          bottomEnd = smallShape.topStart,
        ),
        status = ItemStatus.Fresh,
        count = summary.fresh,
        onClick = { onStatusClick(it) },
      )
      SummaryStatusCard(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(
          topStart = smallShape.topStart,
          topEnd = smallShape.topStart,
          bottomStart = smallShape.topStart,
          bottomEnd = largeShape.topStart,
        ),
        status = ItemStatus.Frozen,
        count = summary.frozen,
        onClick = { onStatusClick(it) },
      )
    }
  }
}

@Composable
private fun DashboardSummaryCardExpanded(
  summary: DashboardSummary,
  onStatusClick: (ItemStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  val arrangement = Arrangement.spacedBy(8.dp)

  val largeShape = CaducityTheme.shapes.largeIncreased
  val smallShape = CaducityTheme.shapes.small

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    horizontalArrangement = arrangement,
  ) {
    SummaryStatusCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(
        topStart = largeShape.topStart,
        topEnd = smallShape.topStart,
        bottomStart = largeShape.topStart,
        bottomEnd = smallShape.topStart,
      ),
      status = ItemStatus.Expired,
      count = summary.expired,
      onClick = { onStatusClick(it) },
    )
    SummaryStatusCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(
        topStart = smallShape.topStart,
        topEnd = smallShape.topStart,
        bottomStart = smallShape.topStart,
        bottomEnd = smallShape.topStart,
      ),
      status = ItemStatus.ExpiringSoon,
      count = summary.expiringSoon,
      onClick = { onStatusClick(it) },
    )
    SummaryStatusCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(
        topStart = smallShape.topStart,
        topEnd = smallShape.topStart,
        bottomStart = smallShape.topStart,
        bottomEnd = smallShape.topStart,
      ),
      status = ItemStatus.Fresh,
      count = summary.fresh,
      onClick = { onStatusClick(it) },
    )
    SummaryStatusCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(
        topStart = smallShape.topStart,
        topEnd = largeShape.topStart,
        bottomStart = smallShape.topStart,
        bottomEnd = largeShape.topStart,
      ),
      status = ItemStatus.Frozen,
      count = summary.frozen,
      onClick = { onStatusClick(it) },
    )
  }
}

@Composable
private fun SummaryStatusCard(
  status: ItemStatus,
  count: Int,
  onClick: (ItemStatus) -> Unit,
  shape: Shape,
  modifier: Modifier = Modifier
) {
  val colors = ExpirationDefaults.getColors(status)

  val dim = if (count > 0) {
    CaducityTheme.dims.noDim
  } else {
    CaducityTheme.dims.dim2
  }

  Surface(
    modifier = Modifier.then(modifier),
    color = colors.container.copy(alpha = dim),
    contentColor = colors.onContainer,
    shape = shape,
  ) {
    Column(
      modifier = Modifier
        .clickable(onClick = { onClick(status) })
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

      val text = when (status) {
        ItemStatus.Expired -> stringResource(R.string.dashboard_section_expired)
        ItemStatus.ExpiringSoon -> stringResource(R.string.dashboard_section_expiring_soon)
        ItemStatus.Fresh -> stringResource(R.string.dashboard_section_fresh)
        ItemStatus.Frozen -> stringResource(R.string.dashboard_section_frozen)
        ItemStatus.Consumed -> stringResource(R.string.dashboard_section_consumed)
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

class ThemeModePreviewParams : CollectionPreviewParameterProvider<ThemeTone>(
  ThemeTone.entries
) {
  override fun getDisplayName(index: Int): String {
    return ThemeTone.entries[index].toString()
  }
}

@PreviewLightDark
@Composable
private fun DashboardSummaryCardPreviewTheme(
  @PreviewParameter(provider = ThemeModePreviewParams::class) themeTone: ThemeTone,
) {
  PreviewTheme {
    CompositionLocalProvider(
      LocalThemeTone provides themeTone,
    ) {
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
}
