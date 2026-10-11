package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedAcUnit: ImageVector? = null

val Symbols.Outlined.AcUnit: ImageVector
  get() =
    _OutlinedAcUnit ?: ImageVector
      .Builder(
        name = "Outlined.AcUnit",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M440-246 337-145q-11 11-27.5 11T282-146q-12-11-12-27.5t12-28.5l158-158v-80h-80L201-281q-11 11-27.5 11T145-282q-11-11-11-27.5t11-27.5l101-103H119q-17 0-28-11.5T80-480q0-17 11.5-28.5T120-520h126L145-622q-11-11-11-27.5t12-28.5q11-11 27.5-11t28.5 11l158 158h80v-80L281-758q-11-11-11-27.5t12-28.5q11-11 27.5-11t27.5 11l103 100v-126q0-17 11.5-28.5T480-880q17 0 28.5 11.5T520-840v126l102-100q11-11 27.5-11t28.5 11q11 12 11 28.5T678-758L520-600v80h80l158-158q11-11 27.5-11t28.5 12q11 11 11 27.5T814-622L714-520h126q17 0 28.5 11.5T880-480q0 17-11.5 28.5T840-440H714l100 103q11 11 11 27.5T814-282q-12 12-28.5 12T758-282L600-440h-80v80l158 159q11 11 11 27.5T677-145q-11 11-27.5 11T622-145L520-246v127q0 17-11.5 28T480-80q-17 0-28.5-11.5T440-120v-126Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedAcUnit = it }

private var _FilledAcUnit: ImageVector? = null

val Symbols.Filled.AcUnit: ImageVector
  get() =
    _FilledAcUnit ?: ImageVector
      .Builder(
        name = "Filled.AcUnit",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M440-246 337-145q-11 11-27.5 11T282-146q-12-11-12-27.5t12-28.5l158-158v-80h-80L201-281q-11 11-27.5 11T145-282q-11-11-11-27.5t11-27.5l101-103H119q-17 0-28-11.5T80-480q0-17 11.5-28.5T120-520h126L145-622q-11-11-11-27.5t12-28.5q11-11 27.5-11t28.5 11l158 158h80v-80L281-758q-11-11-11-27.5t12-28.5q11-11 27.5-11t27.5 11l103 100v-126q0-17 11.5-28.5T480-880q17 0 28.5 11.5T520-840v126l102-100q11-11 27.5-11t28.5 11q11 12 11 28.5T678-758L520-600v80h80l158-158q11-11 27.5-11t28.5 12q11 11 11 27.5T814-622L714-520h126q17 0 28.5 11.5T880-480q0 17-11.5 28.5T840-440H714l100 103q11 11 11 27.5T814-282q-12 12-28.5 12T758-282L600-440h-80v80l158 159q11 11 11 27.5T677-145q-11 11-27.5 11T622-145L520-246v127q0 17-11.5 28T480-80q-17 0-28.5-11.5T440-120v-126Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledAcUnit = it }
