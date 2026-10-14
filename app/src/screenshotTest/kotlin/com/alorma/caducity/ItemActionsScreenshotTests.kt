package com.alorma.caducity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.components.actions.ItemActionsExpiredOneUnitPreview
import com.alorma.caducity.ui.components.actions.ItemActionsExpiredSeveralUnitsPreview
import com.alorma.caducity.ui.components.actions.ItemActionsOneUnitPreview
import com.alorma.caducity.ui.components.actions.ItemActionsSeveralUnitsPreview
import com.android.tools.screenshot.PreviewTest

private const val Width = 411
private const val Height = 760
private const val Night = Configuration.UI_MODE_NIGHT_YES

class ItemActionsScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun itemActionsOneUnit() {
    ItemActionsOneUnitPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun itemActionsSeveralUnits() {
    ItemActionsSeveralUnitsPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun itemActionsExpiredOneUnit() {
    ItemActionsExpiredOneUnitPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun itemActionsExpiredSeveralUnits() {
    ItemActionsExpiredSeveralUnitsPreview()
  }
}
