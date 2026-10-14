package com.alorma.caducity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.ui.screen.status.StatusListPreview
import com.android.tools.screenshot.PreviewTest

private const val Width = 411
private const val Height = 760
private const val Night = Configuration.UI_MODE_NIGHT_YES

class StatusListScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun statusSoon() {
    StatusListPreview(ExpiryStatus.SOON)
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun statusFresh() {
    StatusListPreview(ExpiryStatus.FRESH)
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun statusFrozen() {
    StatusListPreview(ExpiryStatus.FROZEN)
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun statusExpired() {
    StatusListPreview(ExpiryStatus.EXPIRED)
  }
}
