package com.alorma.caducity.ui.screen.settings.components

import android.graphics.Matrix
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

/** Forma que interpola entre dues `RoundedPolygon` (coordenades 0..1 escalades a la mida). */
internal class MorphShape(
  private val morph: Morph,
  private val progress: Float,
) : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    val path: Path = morph.toPath(progress).asComposePath()
    val matrix = Matrix().apply { setScale(size.width, size.height) }
    path.asAndroidPath().transform(matrix)
    return Outline.Generic(path)
  }
}
