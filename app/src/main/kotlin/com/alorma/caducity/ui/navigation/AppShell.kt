package com.alorma.caducity.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.feedback.snackbar.LocalScreenResults
import com.alorma.caducity.ui.components.feedback.snackbar.ScreenResults
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer

/**
 * Space a screen should leave at the end of its scrollable content so the last row can clear the
 * floating bar, which is drawn above the content. Constant on purpose: it must not change while a screen
 * is entering or leaving, or its list would jump when the bar shows or hides.
 */
val LocalFloatingBarPadding = compositionLocalOf { PaddingValues() }

private val FloatingBarContentPadding = PaddingValues(bottom = 96.dp)

/**
 * Parent scaffold of the top-level screens. The [FloatingNavBar] lives in the FAB slot, so it is drawn
 * over [content] (each screen keeps its own scaffold) instead of reserving space below it. It slides
 * down and fades out when the screen on top is not a tab.
 */
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppShell(
  currentTab: TopLevelRoute,
  showNavigation: Boolean,
  onTabSelected: (TopLevelRoute) -> Unit,
  onAddClick: () -> Unit,
  onSettingsClick: () -> Unit,
  screenResults: ScreenResults,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.navigationBars,
    floatingActionButtonPosition = FabPosition.Center,
    floatingActionButton = {
      AnimatedVisibility(
        visible = showNavigation,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
      ) {
        ResponsiveCenteredContainer(fillHeight = false) {
          FloatingNavBar(
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            onAddClick = onAddClick,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.padding(horizontal = 20.dp),
          )
        }
      }
    },
  ) {
    // The scaffold padding is ignored on purpose: the bar overlays the content.
    CompositionLocalProvider(
      LocalFloatingBarPadding provides FloatingBarContentPadding,
      LocalScreenResults provides screenResults,
    ) {
      content()
    }
  }
}
