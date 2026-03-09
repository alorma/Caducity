package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Filled.Sparkles: ImageVector
  get() {
    if (_sparkles != null) {
      return _sparkles!!
    }
    _sparkles =
      ImageVector
        .Builder(
          name = "Filled.Sparkles",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        ).apply {
          path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(19f, 9f)
            lineTo(20.25f, 6.25f)
            lineTo(23f, 5f)
            lineTo(20.25f, 3.75f)
            lineTo(19f, 1f)
            lineTo(17.75f, 3.75f)
            lineTo(15f, 5f)
            lineTo(17.75f, 6.25f)
            close()
            moveTo(19f, 15f)
            lineTo(17.75f, 17.75f)
            lineTo(15f, 19f)
            lineTo(17.75f, 20.25f)
            lineTo(19f, 23f)
            lineTo(20.25f, 20.25f)
            lineTo(23f, 19f)
            lineTo(20.25f, 17.75f)
            close()
            moveTo(11.5f, 9.5f)
            lineTo(9f, 4f)
            lineTo(6.5f, 9.5f)
            lineTo(1f, 12f)
            lineTo(6.5f, 14.5f)
            lineTo(9f, 20f)
            lineTo(11.5f, 14.5f)
            lineTo(17f, 12f)
            close()
          }
        }.build()

    return _sparkles!!
  }

private var _sparkles: ImageVector? = null
