package com.alorma.caducity.base.ui.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.AppIcons

val AppIcons.Outlined.Sparkle: ImageVector
  get() {
    if (_Sparkle != null) return _Sparkle!!

    _Sparkle =
      ImageVector
        .Builder(
          name = "sparkle",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 256f,
          viewportHeight = 256f,
        ).apply {
          path(
            fill = SolidColor(Color.Black),
          ) {
            moveTo(197.58f, 129.06f)
            lineTo(146f, 110f)
            lineToRelative(-19f, -51.62f)
            arcToRelative(15.92f, 15.92f, 0f, false, false, -29.88f, 0f)
            lineTo(78f, 110f)
            lineToRelative(-51.62f, 19f)
            arcToRelative(15.92f, 15.92f, 0f, false, false, 0f, 29.88f)
            lineTo(78f, 178f)
            lineToRelative(19f, 51.62f)
            arcToRelative(15.92f, 15.92f, 0f, false, false, 29.88f, 0f)
            lineTo(146f, 178f)
            lineToRelative(51.62f, -19f)
            arcToRelative(15.92f, 15.92f, 0f, false, false, 0f, -29.88f)
            close()
            moveTo(137f, 164.22f)
            arcToRelative(8f, 8f, 0f, false, false, -4.74f, 4.74f)
            lineTo(112f, 223.85f)
            lineTo(91.78f, 169f)
            arcTo(8f, 8f, 0f, false, false, 87f, 164.22f)
            lineTo(32.15f, 144f)
            lineTo(87f, 123.78f)
            arcTo(8f, 8f, 0f, false, false, 91.78f, 119f)
            lineTo(112f, 64.15f)
            lineTo(132.22f, 119f)
            arcToRelative(8f, 8f, 0f, false, false, 4.74f, 4.74f)
            lineTo(191.85f, 144f)
            close()
            moveTo(144f, 40f)
            arcToRelative(8f, 8f, 0f, false, true, 8f, -8f)
            horizontalLineToRelative(16f)
            verticalLineTo(16f)
            arcToRelative(8f, 8f, 0f, false, true, 16f, 0f)
            verticalLineTo(32f)
            horizontalLineToRelative(16f)
            arcToRelative(8f, 8f, 0f, false, true, 0f, 16f)
            horizontalLineTo(184f)
            verticalLineTo(64f)
            arcToRelative(8f, 8f, 0f, false, true, -16f, 0f)
            verticalLineTo(48f)
            horizontalLineTo(152f)
            arcTo(8f, 8f, 0f, false, true, 144f, 40f)
            close()
            moveTo(248f, 88f)
            arcToRelative(8f, 8f, 0f, false, true, -8f, 8f)
            horizontalLineToRelative(-8f)
            verticalLineToRelative(8f)
            arcToRelative(8f, 8f, 0f, false, true, -16f, 0f)
            verticalLineTo(96f)
            horizontalLineToRelative(-8f)
            arcToRelative(8f, 8f, 0f, false, true, 0f, -16f)
            horizontalLineToRelative(8f)
            verticalLineTo(72f)
            arcToRelative(8f, 8f, 0f, false, true, 16f, 0f)
            verticalLineToRelative(8f)
            horizontalLineToRelative(8f)
            arcTo(8f, 8f, 0f, false, true, 248f, 88f)
            close()
          }
        }.build()

    return _Sparkle!!
  }

private var _Sparkle: ImageVector? = null
