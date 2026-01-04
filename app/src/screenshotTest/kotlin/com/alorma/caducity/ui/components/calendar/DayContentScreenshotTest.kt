package com.alorma.caducity.ui.components.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class DayContentScreenshotTest {

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun DayContentTest(
    @PreviewParameter(provider = DayContentPreviewContentProvider::class) content: DayContentPreviewContent,
  ) {
    DayContentPreview(content = content)
  }
}
