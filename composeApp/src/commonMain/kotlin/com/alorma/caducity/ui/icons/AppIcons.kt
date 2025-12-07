package com.alorma.caducity.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object AppIcons {
  val Dashboard: ImageVector
    get() {
      if (_dashboard != null) {
        return _dashboard!!
      }
      _dashboard = ImageVector.Builder(
        name = "Dashboard",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      ).apply {
        path(
          fill = SolidColor(Color.Black),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          // Top-left square
          moveTo(2f, 2f)
          lineTo(10f, 2f)
          lineTo(10f, 10f)
          lineTo(2f, 10f)
          close()

          // Top-right square
          moveTo(14f, 2f)
          lineTo(22f, 2f)
          lineTo(22f, 10f)
          lineTo(14f, 10f)
          close()

          // Bottom-left square
          moveTo(2f, 14f)
          lineTo(10f, 14f)
          lineTo(10f, 22f)
          lineTo(2f, 22f)
          close()

          // Bottom-right square
          moveTo(14f, 14f)
          lineTo(22f, 14f)
          lineTo(22f, 22f)
          lineTo(14f, 22f)
          close()
        }
      }.build()
      return _dashboard!!
    }

  private var _dashboard: ImageVector? = null

  val Settings: ImageVector
    get() {
      if (_settings != null) {
        return _settings!!
      }
      _settings = ImageVector.Builder(
        name = "Settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      ).apply {
        // Gear teeth
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
          val centerX = 12f
          val centerY = 12f
          val outerRadius = 10f
          val innerRadius = 7f
          val teeth = 8

          var isOuter = true
          for (i in 0 until teeth * 2) {
            val angle = (i * PI / teeth).toFloat()
            val radius = if (isOuter) outerRadius else innerRadius
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)

            if (i == 0) {
              moveTo(x, y)
            } else {
              lineTo(x, y)
            }
            isOuter = !isOuter
          }
          close()
        }

        // Center circle
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
          moveTo(12f, 15f)
          arcToRelative(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -6f)
          arcToRelative(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 6f)
          close()
        }
      }.build()
      return _settings!!
    }

  private var _settings: ImageVector? = null
}