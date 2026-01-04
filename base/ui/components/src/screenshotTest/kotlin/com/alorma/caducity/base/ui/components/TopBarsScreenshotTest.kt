package com.alorma.caducity.base.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.base.ui.theme.preview.AppPreview

class TopBarsScreenshotTest {

  @Preview(name = "Center Aligned Top Bar - Title Only")
  @Composable
  fun CenterAlignedTopBarTitleOnly() {
    AppPreview {
      StyledCenterAlignedTopAppBar(
        title = { Text("Product Details") }
      )
    }
  }

  @Preview(name = "Center Aligned Top Bar - With Navigation")
  @Composable
  fun CenterAlignedTopBarWithNavigation() {
    AppPreview {
      StyledCenterAlignedTopAppBar(
        title = { Text("Product Details") },
        navigationIcon = {
          IconButton(onClick = {}) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  }

  @Preview(name = "Center Aligned Top Bar - Full")
  @Composable
  fun CenterAlignedTopBarFull() {
    AppPreview {
      StyledCenterAlignedTopAppBar(
        title = { Text("Product Details") },
        navigationIcon = {
          IconButton(onClick = {}) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
          }
        }
      )
    }
  }

  @Preview(name = "Standard Top Bar - Title Only")
  @Composable
  fun StandardTopBarTitleOnly() {
    AppPreview {
      StyledTopAppBar(
        title = { Text("Dashboard") }
      )
    }
  }

  @Preview(name = "Standard Top Bar - With Actions")
  @Composable
  fun StandardTopBarWithActions() {
    AppPreview {
      StyledTopAppBar(
        title = { Text("Dashboard") },
        actions = {
          IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
          }
        }
      )
    }
  }
}
