package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedWineBar: ImageVector? = null

val Symbols.Outlined.WineBar: ImageVector
  get() =
    _OutlinedWineBar ?: ImageVector
      .Builder(
        name = "Outlined.WineBar",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M440-200v-164q-86-14-143-80t-57-156v-200q0-17 11.5-28.5T280-840h400q17 0 28.5 11.5T720-800v200q0 90-57 156t-143 80v164h80q17 0 28.5 11.5T640-160q0 17-11.5 28.5T600-120H360q-17 0-28.5-11.5T320-160q0-17 11.5-28.5T360-200h80Zm40-240q56 0 98-34t56-86H326q14 52 56 86t98 34ZM320-640h320v-120H320v120Zm160 200Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedWineBar = it }

private var _FilledWineBar: ImageVector? = null

val Symbols.Filled.WineBar: ImageVector
  get() =
    _FilledWineBar ?: ImageVector
      .Builder(
        name = "Filled.WineBar",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M440-200v-164q-86-14-143-80t-57-156v-200q0-17 11.5-28.5T280-840h400q17 0 28.5 11.5T720-800v200q0 90-57 156t-143 80v164h80q17 0 28.5 11.5T640-160q0 17-11.5 28.5T600-120H360q-17 0-28.5-11.5T320-160q0-17 11.5-28.5T360-200h80ZM320-640h320v-120H320v120Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledWineBar = it }
