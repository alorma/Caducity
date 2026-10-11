package com.alorma.caducity.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/** Radis del sistema de disseny (tokens `radius-*`). */
@Immutable
object CaducityShapes {
  val inner = RoundedCornerShape(6.dp)
  val joint = RoundedCornerShape(8.dp)
  val chip = RoundedCornerShape(12.dp)
  val field = RoundedCornerShape(16.dp)
  val fab = RoundedCornerShape(20.dp)
  val group = RoundedCornerShape(24.dp)
  val card = RoundedCornerShape(28.dp)
  val sheet = RoundedCornerShape(32.dp)
  val full = CircleShape
}

/** `MaterialTheme.shapes`: `extraSmall` = inner (6), `large` = group (24), per al helper de llistes connectades. */
val CaducityMaterialShapes =
  Shapes(
    extraSmall = CaducityShapes.inner,
    small = CaducityShapes.chip,
    medium = CaducityShapes.field,
    large = CaducityShapes.group,
    extraLarge = CaducityShapes.card,
  )
