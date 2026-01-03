package com.alorma.caducity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults

enum class StatusBadgeSize {
  Small,
  Medium,
  Large;

  val dotSize: Dp
    get() = when (this) {
      Small -> 6.dp
      Medium -> 8.dp
      Large -> 10.dp
    }

  val horizontalPadding: Dp
    get() = when (this) {
      Small -> 8.dp
      Medium -> 12.dp
      Large -> 16.dp
    }

  val verticalPadding: Dp
    get() = when (this) {
      Small -> 4.dp
      Medium -> 6.dp
      Large -> 8.dp
    }

  val spacing: Dp
    get() = when (this) {
      Small -> 4.dp
      Medium -> 6.dp
      Large -> 8.dp
    }

  @Composable
  fun textStyle(): TextStyle = when (this) {
    Small -> MaterialTheme.typography.labelSmall
    Medium -> MaterialTheme.typography.labelMedium
    Large -> MaterialTheme.typography.labelLarge
  }
}

@Composable
fun StatusBadge(
  status: InstanceStatus,
  modifier: Modifier = Modifier,
  size: StatusBadgeSize = StatusBadgeSize.Small,
) {
  val colors = ExpirationDefaults.getVibrantColors(status)

  Row(
    modifier = modifier
      .clip(MaterialTheme.shapes.small)
      .background(colors.container)
      .padding(
        horizontal = size.horizontalPadding,
        vertical = size.verticalPadding,
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(
      modifier = Modifier
        .size(size.dotSize)
        .clip(CircleShape)
        .background(colors.onContainer)
    )

    Spacer(modifier = Modifier.width(size.spacing))

    val text = when (status) {
      InstanceStatus.Expired -> stringResource(R.string.expiration_status_badge_expired)
      InstanceStatus.ExpiringSoon -> stringResource(R.string.expiration_status_badge_expiring_soon)
      InstanceStatus.Fresh -> stringResource(R.string.expiration_status_badge_fresh)
      InstanceStatus.Frozen -> stringResource(R.string.expiration_status_badge_frozen)
      InstanceStatus.Consumed -> stringResource(R.string.expiration_status_badge_consumed)
    }

    Text(
      text = text,
      style = size.textStyle(),
      color = colors.onContainer,
    )
  }
}
