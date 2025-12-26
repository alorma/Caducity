package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.ArrowDown: ImageVector
  get() {
    if (_arrowDown != null) {
      return _arrowDown!!
    }
    _arrowDown = ImageVector.Builder(
      name = "ArrowDown",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(
        fill = null,
        fillAlpha = 1.0f,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        // Arrow pointing down
        moveTo(6f, 9f)
        lineTo(12f, 15f)
        lineTo(18f, 9f)
      }
    }.build()
    return _arrowDown!!
  }

private var _arrowDown: ImageVector? = null

val AppIcons.ArrowUp: ImageVector
  get() {
    if (_arrowUp != null) {
      return _arrowUp!!
    }
    _arrowUp = ImageVector.Builder(
      name = "ArrowUp",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(
        fill = null,
        fillAlpha = 1.0f,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        // Arrow pointing up
        moveTo(18f, 15f)
        lineTo(12f, 9f)
        lineTo(6f, 15f)
      }
    }.build()
    return _arrowUp!!
  }

private var _arrowUp: ImageVector? = null

val AppIcons.Back: ImageVector
  get() {
    if (_back != null) {
      return _back!!
    }
    _back = ImageVector.Builder(
      name = "Back",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(
        fill = null,
        fillAlpha = 1.0f,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        // Arrow pointing left
        moveTo(15f, 18f)
        lineTo(9f, 12f)
        lineTo(15f, 6f)
      }
    }.build()
    return _back!!
  }

private var _back: ImageVector? = null
