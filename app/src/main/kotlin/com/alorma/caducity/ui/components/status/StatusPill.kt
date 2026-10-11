package com.alorma.caducity.ui.components.status

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.StatusColors
import com.alorma.caducity.ui.theme.colors.colors

/** Pastilla d'estat: fons `*-container`, text `on-*-container`. L'etiqueta sempre és text, mai només color. */
@Composable
fun StatusPill(
  status: ExpiryStatus,
  label: String,
  modifier: Modifier = Modifier,
) {
  val colors = status.colors()
  StatusPill(
    label = label,
    container = colors.container,
    onContainer = colors.onContainer,
    modifier = modifier,
  )
}

@Composable
fun StatusPill(
  label: String,
  statusColors: StatusColors,
  modifier: Modifier = Modifier,
) {
  StatusPill(
    label = label,
    container = statusColors.container,
    onContainer = statusColors.onContainer,
    modifier = modifier,
  )
}

@Composable
fun StatusPill(
  label: String,
  container: Color,
  onContainer: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = CaducityShapes.chip,
    color = container,
    contentColor = onContainer,
  ) {
    Text(
      text = label,
      style = CaducityTheme.typography.labelMedium,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
  }
}
