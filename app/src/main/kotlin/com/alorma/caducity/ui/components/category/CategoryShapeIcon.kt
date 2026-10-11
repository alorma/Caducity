package com.alorma.caducity.ui.components.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityTheme

/**
 * Icona de categoria: forma orgànica farcida de `surface-shape` amb la icona en `primary`.
 * Seleccionada: forma `primary`, icona `on-primary`. Mides típiques: 28, 36, 44, 56, 80, 120.
 */
@Composable
fun CategoryShapeIcon(
  shape: CategoryShape,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  selected: Boolean = false,
  contentDescription: String? = null,
) {
  val container = if (selected) CaducityTheme.colorScheme.primary else CaducityTheme.colors.surfaceShape
  val content = if (selected) CaducityTheme.colorScheme.onPrimary else CaducityTheme.colorScheme.primary
  Box(
    modifier =
      modifier
        .size(size)
        .clip(shape.toComposeShape())
        .background(container),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = content,
      modifier = Modifier.size(size * 0.5f),
    )
  }
}
