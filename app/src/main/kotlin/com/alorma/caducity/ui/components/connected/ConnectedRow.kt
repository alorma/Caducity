package com.alorma.caducity.ui.components.connected

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.shape.connectedShape
import com.alorma.caducity.ui.theme.CaducityTheme

/** Separació entre files d'una llista connectada. */
val ConnectedListSpacing: Dp = 2.dp

/**
 * Fila d'una llista connectada: les files formen una sola peça (24 dp fora, 6 dp dins).
 * Col·loca-les en una `Column`/`LazyColumn` amb `Arrangement.spacedBy(ConnectedListSpacing)`.
 */
@Composable
fun ConnectedRow(
  index: Int,
  count: Int,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
  content: @Composable RowScope.() -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = connectedShape(index, count),
    color = CaducityTheme.colorScheme.surfaceContainer,
    contentColor = CaducityTheme.colorScheme.onSurface,
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
          .padding(contentPadding),
      verticalAlignment = Alignment.CenterVertically,
      content = content,
    )
  }
}
