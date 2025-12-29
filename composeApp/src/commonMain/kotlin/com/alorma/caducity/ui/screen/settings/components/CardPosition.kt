package com.alorma.caducity.ui.screen.settings.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import com.alorma.caducity.base.ui.theme.CaducityTheme

/**
 * Defines the position of a card within a group of cards.
 * Used to automatically apply appropriate corner shapes.
 */
enum class CardPosition {
  /** Single card (all corners rounded) */
  Single,

  /** First card in a group (top corners rounded) */
  Top,

  /** Middle card in a group (no corners rounded) */
  Middle,

  /** Last card in a group (bottom corners rounded) */
  Bottom
}

/**
 * Returns the appropriate shape based on the card's position in a group.
 */
@Suppress("ContentEmission")
@Composable
fun CardPosition.toShape(): Shape {
  val largeShape = CaducityTheme.shapes.large
  val smallShape = CaducityTheme.shapes.extraSmall

  return when (this) {
    CardPosition.Single -> largeShape
    CardPosition.Top -> RoundedCornerShape(
      topStart = largeShape.topStart,
      topEnd = largeShape.topEnd,
      bottomStart = smallShape.bottomStart,
      bottomEnd = smallShape.bottomEnd,
    )

    CardPosition.Middle -> smallShape
    CardPosition.Bottom -> RoundedCornerShape(
      topStart = smallShape.topStart,
      topEnd = smallShape.topEnd,
      bottomStart = largeShape.bottomStart,
      bottomEnd = largeShape.bottomEnd,
    )
  }
}
