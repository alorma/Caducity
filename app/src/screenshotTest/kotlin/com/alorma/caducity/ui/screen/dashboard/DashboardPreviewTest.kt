package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class DashboardPreviewTest {

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun DashboardSuccessContentPreviewTest(
    @PreviewParameter(provider = DashboardPreviewProvider::class) state: DashboardState,
  ) {
    DashboardSuccessContentPreview(state)
  }

}