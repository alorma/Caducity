package com.alorma.caducity.ui.components.connected

import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.alorma.caducity.ui.theme.CaducityTheme

/** Separació entre files d'una llista connectada (la de `SegmentedListItem`). */
val ConnectedListSpacing: Dp = ListItemDefaults.SegmentedGap

/** Colors d'una fila de `SegmentedListItem` amb els tons de l'app. */
@Composable
fun connectedRowColors(
  containerColor: Color = CaducityTheme.colorScheme.surfaceContainer,
  contentColor: Color = CaducityTheme.colorScheme.onSurface,
): ListItemColors =
  ListItemDefaults.segmentedColors(
    containerColor = containerColor,
    contentColor = contentColor,
    selectedContainerColor = containerColor,
    selectedContentColor = contentColor,
  )
