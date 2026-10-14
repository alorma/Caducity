package com.alorma.caducity.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@PreviewDynamicLightDark
@Composable
internal fun FloatingNavBarHomePreview() {
  NavBarPreview(Home)
}

@PreviewDynamicLightDark
@Composable
internal fun FloatingNavBarPantryPreview() {
  NavBarPreview(Pantry)
}

@Composable
private fun NavBarPreview(currentTab: TopLevelRoute) {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.background) {
      FloatingNavBar(
        currentTab = currentTab,
        onTabSelected = {},
        onAddClick = {},
        onSettingsClick = {},
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
      )
    }
  }
}
