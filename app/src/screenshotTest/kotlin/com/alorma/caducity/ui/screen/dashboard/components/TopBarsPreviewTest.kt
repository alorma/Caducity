package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.base.ui.components.StyledCenterAlignedTopAppBarPreview
import com.alorma.caducity.base.ui.components.StyledTopAppBarPreview
import com.android.tools.screenshot.PreviewTest

@PreviewTest
@Preview
@Composable
fun StyledTopBarTest() {
  StyledTopAppBarPreview()
}

@PreviewTest
@Preview
@Composable
fun StyledCenterAlignedTest() {
  StyledCenterAlignedTopAppBarPreview()
}
