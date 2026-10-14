package com.alorma.caducity.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHost
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarResult
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbarEvents
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import kotlinx.coroutines.launch

/**
 * Space a screen should leave at the end of its scrollable content so the last row can clear the
 * floating bar, which is drawn above the content. Constant on purpose: it must not change while a screen
 * is entering or leaving, or its list would jump when the bar shows or hides.
 */
val LocalFloatingBarPadding = compositionLocalOf { PaddingValues() }

private val FloatingBarContentPadding = PaddingValues(bottom = 96.dp)

/**
 * Parent scaffold of the top-level screens. The [FloatingNavBar] lives in the FAB slot, so it is drawn
 * over [content] (each screen keeps its own scaffold) instead of reserving space below it.
 */
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppShell(
  currentTab: TopLevelRoute,
  showNavigation: Boolean,
  onTabSelected: (TopLevelRoute) -> Unit,
  onAddClick: () -> Unit,
  onSettingsClick: () -> Unit,
  snackbarEvents: GlobalSnackbarEvents,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val snackbarState = rememberAppSnackbarState()
  val resources by rememberUpdatedState(LocalResources.current)
  LaunchedEffect(snackbarEvents) {
    snackbarEvents.events.collect { event ->
      launch {
        val result =
          snackbarState.showSnackbar(
            message = resources.getString(event.message, *event.messageArgs.toTypedArray()),
            actionLabel = event.actionLabel?.let(resources::getString),
            type = event.type,
          )
        if (result == AppSnackbarResult.ActionPerformed) event.onAction?.invoke()
      }
    }
  }

  Scaffold(
    modifier = modifier,
    snackbarHost = { AppSnackbarHost(snackbarState) },
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.navigationBars,
    floatingActionButtonPosition = FabPosition.Center,
    floatingActionButton = {
      if (showNavigation) {
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
    CompositionLocalProvider(LocalFloatingBarPadding provides FloatingBarContentPadding) {
      content()
    }
  }
}
