package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedNutrition: ImageVector? = null

val Symbols.Outlined.Nutrition: ImageVector
  get() =
    _OutlinedNutrition ?: ImageVector
      .Builder(
        name = "Outlined.Nutrition",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M480-120q-117 0-198.5-81.5T200-400q0-94 55.5-168.5T401-669q-20-5-39-14.5T328-708q-33-33-42.5-78.5T281-879q47-5 92.5 4.5T452-832q23 23 33.5 52t13.5 61q13-31 31.5-58.5T572-828q11-11 28-11t28 11q11 11 11 28t-11 28q-22 22-39 48.5T564-667q88 28 142 101.5T760-400q0 117-81.5 198.5T480-120Zm0-80q83 0 141.5-58.5T680-400q0-83-58.5-141.5T480-600q-83 0-141.5 58.5T280-400q0 83 58.5 141.5T480-200Zm0-200Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedNutrition = it }

private var _FilledNutrition: ImageVector? = null

val Symbols.Filled.Nutrition: ImageVector
  get() =
    _FilledNutrition ?: ImageVector
      .Builder(
        name = "Filled.Nutrition",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M480-120q-117 0-198.5-81.5T200-400q0-94 55.5-168.5T401-669q-33-8-57.5-28.5T304-747q-15-29-21-63t-2-69q41-5 78 5t67 32q30 22 49.5 53t23.5 70q13-31 31.5-58.5T572-828q11-11 28-11t28 11q11 11 11 28t-11 28q-22 22-39 48.5T564-667q88 28 142 101.5T760-400q0 117-81.5 198.5T480-120Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledNutrition = it }
