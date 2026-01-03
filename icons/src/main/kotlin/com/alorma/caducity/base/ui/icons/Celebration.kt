package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Filled.Celebration: ImageVector
  get() {
    if (_celebration != null) {
      return _celebration!!
    }
    _celebration = ImageVector.Builder(
      name = "Filled.Celebration",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(fill = SolidColor(Color(0xFF000000))) {
        moveTo(12f, 2f)
        lineTo(14f, 8f)
        lineTo(20f, 10f)
        lineTo(14f, 12f)
        lineTo(12f, 18f)
        lineTo(10f, 12f)
        lineTo(4f, 10f)
        lineTo(10f, 8f)
        close()
        moveTo(12f, 11f)
        curveTo(12.55f, 11f, 13f, 10.55f, 13f, 10f)
        curveTo(13f, 9.45f, 12.55f, 9f, 12f, 9f)
        curveTo(11.45f, 9f, 11f, 9.45f, 11f, 10f)
        curveTo(11f, 10.55f, 11.45f, 11f, 12f, 11f)
        close()
        moveTo(17f, 16f)
        lineTo(18.5f, 19f)
        lineTo(22f, 20f)
        lineTo(18.5f, 21f)
        lineTo(17f, 24f)
        lineTo(15.5f, 21f)
        lineTo(12f, 20f)
        lineTo(15.5f, 19f)
        close()
        moveTo(6f, 16f)
        lineTo(7f, 18f)
        lineTo(9f, 19f)
        lineTo(7f, 20f)
        lineTo(6f, 22f)
        lineTo(5f, 20f)
        lineTo(3f, 19f)
        lineTo(5f, 18f)
        close()
      }
    }.build()

    return _celebration!!
  }

private var _celebration: ImageVector? = null
