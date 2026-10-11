package com.alorma.caducity.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer

/**
 * Shell shown over the top-level screens: [content] plus the [FloatingNavBar].
 * The bar is always visible and sits below the content, so it never covers it while scrolling.
 */
@Composable
fun AppShell(
  currentTab: TopLevelRoute,
  showNavigation: Boolean,
  onTabSelected: (TopLevelRoute) -> Unit,
  onAddClick: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
  ) {
    Box(modifier = Modifier.weight(1f)) {
      content()
    }
    if (showNavigation) {
      ResponsiveCenteredContainer(fillHeight = false) {
        FloatingNavBar(
          currentTab = currentTab,
          onTabSelected = onTabSelected,
          onAddClick = onAddClick,
          modifier =
            Modifier
              .windowInsetsPadding(WindowInsets.navigationBars)
              .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        )
      }
    }
  }
}
