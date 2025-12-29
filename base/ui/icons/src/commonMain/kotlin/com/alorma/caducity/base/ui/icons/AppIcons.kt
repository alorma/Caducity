package com.alorma.caducity.base.ui.icons

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

  val Notifications: ImageVector
    get() {
      if (_notifications != null) {
        return _notifications!!
      }
      _notifications = ImageVector.Builder(
        name = "Notifications",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      ).apply {
        // Bell body
        path(
          fill = null,
          stroke = SolidColor(Color.Black),
          strokeAlpha = 1.0f,
          strokeLineWidth = 2f,
          strokeLineCap = StrokeCap.Round,
          strokeLineJoin = StrokeJoin.Round,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(18f, 8f)
          curveTo(18f, 6.4087f, 17.3679f, 4.8826f, 16.2426f, 3.7574f)
          curveTo(15.1174f, 2.6321f, 13.5913f, 2f, 12f, 2f)
          curveTo(10.4087f, 2f, 8.8826f, 2.6321f, 7.7574f, 3.7574f)
          curveTo(6.6321f, 4.8826f, 6f, 6.4087f, 6f, 8f)
          curveTo(6f, 15f, 3f, 17f, 3f, 17f)
          lineTo(21f, 17f)
          curveTo(21f, 17f, 18f, 15f, 18f, 8f)
          close()
        }
        // Bell clapper
        path(
          fill = null,
          stroke = SolidColor(Color.Black),
          strokeAlpha = 1.0f,
          strokeLineWidth = 2f,
          strokeLineCap = StrokeCap.Round,
          strokeLineJoin = StrokeJoin.Round,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(13.73f, 21f)
          curveTo(13.5542f, 21.3031f, 13.3019f, 21.5547f, 12.9982f, 21.7295f)
          curveTo(12.6946f, 21.9044f, 12.3504f, 21.9965f, 12f, 21.9965f)
          curveTo(11.6496f, 21.9965f, 11.3054f, 21.9044f, 11.0018f, 21.7295f)
          curveTo(10.6982f, 21.5547f, 10.4458f, 21.3031f, 10.27f, 21f)
        }
      }.build()
      return _notifications!!
    }

  private var _notifications: ImageVector? = null

  val Info: ImageVector
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
}