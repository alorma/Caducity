package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedTakeoutDining: ImageVector? = null

val Symbols.Outlined.TakeoutDining: ImageVector
  get() =
    _OutlinedTakeoutDining ?: ImageVector
      .Builder(
        name = "Outlined.TakeoutDining",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData = addPathNodes("M312-240h338l19-280H292l20 280Zm-26-360h389l3-50-112-110H394L282-650l4 50Zm-76 68L108-634q-11-11-11-28t11-28q11-11 28-11t28 11l38 38v-8q2-13 7.5-24t14.5-20l113-113q11-11 25.5-17t30.5-6h174q16 0 30.5 6t25.5 17l113 113q9 9 14.5 20.5T758-659v7l38-38q11-11 28-11t28 11q11 11 11 28t-11 28L750-532H210Zm438 372H312q-32 0-55-21t-25-53l-22-298h540l-22 298q-2 32-25 53t-55 21ZM480-600Zm1 80Z"),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedTakeoutDining = it }

private var _FilledTakeoutDining: ImageVector? = null

val Symbols.Filled.TakeoutDining: ImageVector
  get() =
    _FilledTakeoutDining ?: ImageVector
      .Builder(
        name = "Filled.TakeoutDining",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData = addPathNodes("m185-560-77-74q-12-11-11.5-28t11.5-28q11-11 28-11t28 11l38 38v-8q2-13 7.5-24t14.5-20l113-113q11-11 25.5-17t30.5-6h174q16 0 30.5 6t25.5 17l113 113q9 9 14.5 20.5T758-659v7l38-38q11-11 28-11t28 11q12 12 11.5 28.5T851-634l-76 74H185Zm463 400H312q-32 0-55-21t-25-53l-20-264h536l-20 264q-2 32-25 53t-55 21Z"),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledTakeoutDining = it }
