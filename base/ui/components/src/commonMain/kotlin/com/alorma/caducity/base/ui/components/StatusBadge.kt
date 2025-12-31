package com.alorma.caducity.base.ui.components

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
import androidx.compose.ui.unit.dp
import caducity.base.ui.components.generated.resources.Res
import caducity.base.ui.components.generated.resources.expiration_status_badge_expired
import caducity.base.ui.components.generated.resources.expiration_status_badge_expiring_soon
import caducity.base.ui.components.generated.resources.expiration_status_badge_fresh
import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.base.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.base.ui.theme.CaducityTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun StatusBadge(
  status: InstanceStatus,
  modifier: Modifier = Modifier,
) {
  val colors = ExpirationDefaults.getColors(status)

  Row(
    modifier = modifier
      .clip(MaterialTheme.shapes.small)
      .background(colors.container.copy(CaducityTheme.dims.dim3))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(colors.onContainer)
    )

    Spacer(modifier = Modifier.width(4.dp))

    val text = when (status) {
      InstanceStatus.Expired -> stringResource(Res.string.expiration_status_badge_expired)
      InstanceStatus.ExpiringSoon -> stringResource(Res.string.expiration_status_badge_expiring_soon)
      InstanceStatus.Fresh -> stringResource(Res.string.expiration_status_badge_fresh)
    }

    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = colors.onContainer,
    )
  }
}
