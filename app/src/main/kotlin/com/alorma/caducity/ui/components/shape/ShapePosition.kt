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
  End;
}

/**
 * Calculates the shape position for an item considering gaps in a sequence.
 * Use this when iterating through a list where not all items should be visually connected.
 *
 * @param index The index in the list to calculate the shape for
 * @param hasContent Predicate to determine if an item at given index should be visually connected
 * @return The appropriate ShapePosition based on adjacent items with content
 */
fun <T> List<T>.calculateShapeWithGaps(
  index: Int,
  hasContent: (T) -> Boolean = { true }
): ShapePosition {
  val current = getOrNull(index) ?: return ShapePosition.None
  val currentHasContent = hasContent(current)

  if (!currentHasContent) return ShapePosition.None

  val hasPrevious = getOrNull(index - 1)?.let(hasContent) == true
  val hasNext = getOrNull(index + 1)?.let(hasContent) == true

  return when {
    !hasPrevious && !hasNext -> ShapePosition.Single
    !hasPrevious && hasNext -> ShapePosition.Start
    hasPrevious && !hasNext -> ShapePosition.End
    else -> ShapePosition.Middle
  }
}

@Suppress("ContentEmission")
@Composable
fun ShapePosition.toVerticalShape(): Shape {
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
fun ShapePosition.toHorizontalShape(
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
