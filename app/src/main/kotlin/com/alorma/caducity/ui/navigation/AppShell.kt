package com.alorma.caducity.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer

/** Bottom space screens inside the shell should reserve so their last row clears the bar and FAB. */
val ShellContentBottomPadding: PaddingValues = PaddingValues(bottom = 112.dp)

/**
 * Shell shown over the top-level screens: [content] plus the [FloatingNavBar].
 * The bar hides on scroll down and comes back on scroll up.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppShell(
  currentTab: TopLevelRoute,
  showNavigation: Boolean,
  onTabSelected: (TopLevelRoute) -> Unit,
  onAddClick: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val scrollBehavior =
    FloatingToolbarDefaults.exitAlwaysScrollBehavior(
      exitDirection = FloatingToolbarExitDirection.Bottom,
    )
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior),
  ) {
    content()
    if (showNavigation) {
      ResponsiveCenteredContainer {
        Box(modifier = Modifier.fillMaxSize()) {
          FloatingNavBar(
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            onAddClick = onAddClick,
            scrollBehavior = scrollBehavior,
            modifier =
              Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
          )
        }
      }
    }
  }
}
