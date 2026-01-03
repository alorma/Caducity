package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Backup: ImageVector
  get() {
    if (_backup != null) {
      return _backup!!
    }
    _backup = ImageVector.Builder(
      name = "Backup",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Cloud shape
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(19f, 18f)
        horizontalLineTo(6f)
        arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.5f, -7.93f)
        arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.5f, -2.57f)
        arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.5f, 1f)
        arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.5f, 5.93f)
        close()
      }
      // Arrow down
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(12f, 12f)
        lineToRelative(0f, 6f)
      }
      // Arrow left side
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(9f, 15f)
        lineToRelative(3f, 3f)
      }
      // Arrow right side
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(15f, 15f)
        lineToRelative(-3f, 3f)
      }
    }.build()
    return _backup!!
  }

private var _backup: ImageVector? = null
