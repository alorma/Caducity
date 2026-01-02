package com.alorma.caducity.ui.components.shape

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.alorma.caducity.ui.theme.CaducityTheme

enum class ShapePosition {
  None,
  Single,
  Start,
  Middle,
  End
}

@Suppress("ContentEmission")
@Composable
fun ShapePosition.toCardShape(): Shape {
  val largeShape = CaducityTheme.shapes.large
  val smallShape = CaducityTheme.shapes.extraSmall

  return when (this) {
    ShapePosition.None -> RectangleShape
    ShapePosition.Single -> largeShape
    ShapePosition.Start -> RoundedCornerShape(
      topStart = largeShape.topStart,
      topEnd = largeShape.topEnd,
      bottomStart = smallShape.bottomStart,
      bottomEnd = smallShape.bottomEnd,
    )

    ShapePosition.Middle -> smallShape
    ShapePosition.End -> RoundedCornerShape(
      topStart = smallShape.topStart,
      topEnd = smallShape.topEnd,
      bottomStart = largeShape.bottomStart,
      bottomEnd = largeShape.bottomEnd,
    )
  }
}

@Suppress("ContentEmission")
@Composable
fun ShapePosition.toCalendarShape(
  externalBaseShape: CornerBasedShape = CaducityTheme.shapes.large,
  internalBaseShape: CornerBasedShape = CaducityTheme.shapes.extraSmall,
): CornerBasedShape {

  return when (this) {
    ShapePosition.None -> externalBaseShape
    ShapePosition.Single -> externalBaseShape
    ShapePosition.Start -> RoundedCornerShape(
      topStart = externalBaseShape.topStart,
      topEnd = internalBaseShape.topEnd,
      bottomStart = externalBaseShape.bottomStart,
      bottomEnd = internalBaseShape.bottomEnd,
    )

    ShapePosition.Middle -> internalBaseShape
    ShapePosition.End -> RoundedCornerShape(
      topStart = internalBaseShape.topStart,
      topEnd = externalBaseShape.topEnd,
      bottomStart = internalBaseShape.bottomStart,
      bottomEnd = externalBaseShape.bottomEnd,
    )
  }
}
