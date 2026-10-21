package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedTag: ImageVector? = null

val Symbols.Outlined.Tag: ImageVector
  get() =
    _OutlinedTag ?: ImageVector
      .Builder(
        name = "Outlined.Tag",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "m360-320-33 131q-3 13-13 21t-24 8q-19 0-31-15t-7-33l28-112H171q-20 0-32-15.5t-7-34.5q3-14 14-22t25-8h129l40-160H231q-20 0-32-15.5t-7-34.5q3-14 14-22t25-8h129l33-131q3-13 13-21t24-8q19 0 31 15t7 33l-28 112h160l33-131q3-13 13-21t24-8q19 0 31 15t7 33l-28 112h109q20 0 32 15.5t7 34.5q-3 14-14 22t-25 8H660l-40 160h109q20 0 32 15.5t7 34.5q-3 14-14 22t-25 8H600l-33 131q-3 13-13 21t-24 8q-19 0-31-15t-7-33l28-112H360Zm20-80h160l40-160H420l-40 160Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedTag = it }

private var _FilledTag: ImageVector? = null

val Symbols.Filled.Tag: ImageVector
  get() =
    _FilledTag ?: ImageVector
      .Builder(
        name = "Filled.Tag",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "m360-320-33 131q-3 13-13 21t-24 8q-19 0-31-15t-7-33l28-112H171q-20 0-32-15.5t-7-34.5q3-14 14-22t25-8h129l40-160H231q-20 0-32-15.5t-7-34.5q3-14 14-22t25-8h129l33-131q3-13 13-21t24-8q19 0 31 15t7 33l-28 112h160l33-131q3-13 13-21t24-8q19 0 31 15t7 33l-28 112h109q20 0 32 15.5t7 34.5q-3 14-14 22t-25 8H660l-40 160h109q20 0 32 15.5t7 34.5q-3 14-14 22t-25 8H600l-33 131q-3 13-13 21t-24 8q-19 0-31-15t-7-33l28-112H360Zm20-80h160l40-160H420l-40 160Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledTag = it }
