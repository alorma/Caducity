package com.alorma.caducity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.screen.category.CategoryPreviewContent
import com.alorma.caducity.ui.screen.category.editor.CategoryEditorPreviewContent
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.android.tools.screenshot.PreviewTest

private const val Width = 411
private const val Height = 1100
private const val Night = Configuration.UI_MODE_NIGHT_YES

class CategoryScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun category() {
    PreviewTheme { CategoryPreviewContent() }
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun newCategory() {
    PreviewTheme { CategoryEditorPreviewContent() }
  }
}
