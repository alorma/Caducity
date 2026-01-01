package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Language: ImageVector
  get() {
    if (_language != null) {
      return _language!!
    }
    _language = ImageVector.Builder(
      name = "Language",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Globe circle
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
      // Vertical line through center
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(12f, 2f)
        lineTo(12f, 22f)
      }
      // Top horizontal arc
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(2f, 12f)
        lineTo(22f, 12f)
      }
      // Ellipse overlay for globe effect
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(12f, 2f)
        arcTo(5f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 22f)
        arcTo(5f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 2f)
        close()
      }
    }.build()
    return _language!!
  }

private var _language: ImageVector? = null
