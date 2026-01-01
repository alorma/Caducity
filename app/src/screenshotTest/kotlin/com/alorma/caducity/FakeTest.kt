package com.alorma.caducity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest

class FakeTest {
  @Preview
  @PreviewTest
  @Composable
  fun fake() {
    Box(
      modifier = Modifier
        .size(100.dp)
        .background(Color.Red),
    ) {
      Text(text = "Test")
    }
  }
}