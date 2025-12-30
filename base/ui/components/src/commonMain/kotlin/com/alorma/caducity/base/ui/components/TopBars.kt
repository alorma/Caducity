package com.alorma.caducity.base.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.base.ui.theme.topBar

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
    title = { StyledTitle(title) },
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
    title = { StyledTitle(title) },
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
private fun StyledTitle(title: @Composable (() -> Unit)) {
  CompositionLocalProvider(
    LocalTextStyle provides LocalTextStyle.current.topBar()
  ) {
    title()
  }
}

@Preview
@Composable
fun StyledTitlePreview() {
  AppPreview {
    StyledTitle {
      Text(text = "Preview")
    }
  }
}

@Preview
@Composable
fun StyledCenterAlignedTopAppBarPreview() {
  AppPreview {
    StyledCenterAlignedTopAppBar(title = {
      Text(text = "Preview")
    })
  }
}

@Preview
@Composable
fun StyledTopAppBarPreview() {
  AppPreview {
    StyledTopAppBar(title = {
      Text(text = "Preview")
    })
  }
}