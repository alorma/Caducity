package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Info: ImageVector
  get() {
    if (_info != null) {
      return _info!!
    }
    _info = ImageVector.Builder(
      name = "Info",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Circle outline
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(12f, 22f)
        arcToRelative(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -20f)
        arcToRelative(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 20f)
        close()
      }
      // Info line
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(12f, 16f)
        lineTo(12f, 12f)
      }
      // Info dot
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(12f, 8f)
        arcToRelative(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 2f)
        arcToRelative(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -2f)
        close()
      }
    }.build()
    return _info!!
  }

private var _info: ImageVector? = null
