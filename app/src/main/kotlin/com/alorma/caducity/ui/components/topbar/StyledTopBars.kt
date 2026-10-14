package com.alorma.caducity.ui.components.topbar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@Composable
fun NavigationIcon() {
  val localBackPress = LocalOnBackPressedDispatcherOwner.current

  IconButton(
    onClick = {
      localBackPress?.onBackPressedDispatcher?.onBackPressed()
    },
  ) {
    Icon(
      imageVector = AppIcons.Back,
      contentDescription = null,
    )
  }
}

@Composable
fun StyledTopAppBar(
  title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  navigationIcon: @Composable () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
  scrollBehavior: TopAppBarScrollBehavior? = null,
  contentPadding: PaddingValues = TopAppBarDefaults.ContentPadding,
) {
  ResponsiveCenteredContainer(fillHeight = false) {
    TopAppBar(
      title = title,
      modifier = modifier,
      navigationIcon = navigationIcon,
      actions = actions,
      expandedHeight = expandedHeight,
      windowInsets = windowInsets,
      colors = colors,
      scrollBehavior = scrollBehavior,
      contentPadding = contentPadding,
    )
  }
}

/** Large collapsing app bar with subtitle (Home, Pantry, Category); it folds into a regular one on scroll. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StyledLargeTopAppBar(
  title: @Composable () -> Unit,
  subtitle: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  navigationIcon: @Composable () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
  scrollBehavior: TopAppBarScrollBehavior? = null,
) {
  ResponsiveCenteredContainer(fillHeight = false) {
    LargeFlexibleTopAppBar(
      title = title,
      modifier = modifier,
      subtitle = subtitle,
      navigationIcon = navigationIcon,
      actions = actions,
      colors = colors,
      scrollBehavior = scrollBehavior,
    )
  }
}

@PreviewLightDark
@Composable
private fun StyledTopBarPreview() {
  PreviewTheme {
    StyledTopAppBarPreviewContent()
  }
}

@Composable
fun StyledTopAppBarPreviewContent() {
  StyledTopAppBar(
    title = { Text(text = "Preview") },
  )
}
