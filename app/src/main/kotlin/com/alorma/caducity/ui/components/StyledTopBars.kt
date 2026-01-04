package com.alorma.caducity.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.alorma.caducity.ui.theme.preview.ScreenshotPreviewTheme

@Composable
fun StyledCenterAlignedTopAppBar(
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
  CenterAlignedTopAppBar(
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

@PreviewLightDark
@Composable
private fun StyledCenterAlignedTopBarPreview() {
  PreviewTheme {
    StyledCenterAlignedTopBarPreviewContent()
  }
}

@Composable
fun StyledCenterAlignedTopBarScreenshot() {
  PreviewTheme {
    StyledCenterAlignedTopBarPreviewContent()
  }
}

@Composable
fun StyledCenterAlignedTopBarPreviewContent() {
  StyledCenterAlignedTopAppBar(
    title = { Text(text = "Preview") },
  )
}

@PreviewLightDark
@Composable
private fun StyledTopBarPreview() {
  PreviewTheme {
    StyledTopAppBarPreviewContent()
  }
}

@Composable
 fun StyledTopBarScreenshot() {
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