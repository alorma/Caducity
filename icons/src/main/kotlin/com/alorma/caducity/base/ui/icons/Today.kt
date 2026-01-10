package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Today: ImageVector
  get() {
    if (_today != null) {
      return _today!!
    }
    _today = ImageVector.Builder(
      name = "Today",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Calendar vertical lines (top hanging)
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
      ) {
        moveTo(8f, 2f)
        verticalLineToRelative(4f)
      }

      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
      ) {
        moveTo(16f, 2f)
        verticalLineToRelative(4f)
      }

      // Calendar rectangle with rounded corners
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
      ) {
        moveTo(5f, 4f)
        horizontalLineTo(19f)
        arcTo(2f, 2f, 0f, false, true, 21f, 6f)
        verticalLineTo(20f)
        arcTo(2f, 2f, 0f, false, true, 19f, 22f)
        horizontalLineTo(5f)
        arcTo(2f, 2f, 0f, false, true, 3f, 20f)
        verticalLineTo(6f)
        arcTo(2f, 2f, 0f, false, true, 5f, 4f)
        close()
      }

      // Top bar separator
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
      ) {
        moveTo(3f, 10f)
        horizontalLineToRelative(18f)
      }

      // Circle/dot to represent "today"
      path(
        fill = SolidColor(Color.Black),
        stroke = null
      ) {
        moveTo(12f, 16f)
        arcTo(2f, 2f, 0f, true, true, 12f, 12f)
        arcTo(2f, 2f, 0f, true, true, 12f, 16f)
        close()
      }
    }.build()
    return _today!!
  }

private var _today: ImageVector? = null
