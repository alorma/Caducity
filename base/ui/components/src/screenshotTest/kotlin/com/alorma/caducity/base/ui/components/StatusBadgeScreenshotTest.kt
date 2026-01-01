package com.alorma.caducity.base.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.base.ui.theme.preview.AppPreview

class StatusBadgeScreenshotTest {

  @Preview(name = "Fresh Status - Small")
  @Composable
  fun StatusBadgeFreshSmall() {
    AppPreview {
      StatusBadge(
        status = InstanceStatus.Fresh,
        size = StatusBadgeSize.Small
      )
    }
  }

  @Preview(name = "Expiring Soon Status - Medium")
  @Composable
  fun StatusBadgeExpiringSoonMedium() {
    AppPreview {
      StatusBadge(
        status = InstanceStatus.ExpiringSoon,
        size = StatusBadgeSize.Medium
      )
    }
  }

  @Preview(name = "Expired Status - Large")
  @Composable
  fun StatusBadgeExpiredLarge() {
    AppPreview {
      StatusBadge(
        status = InstanceStatus.Expired,
        size = StatusBadgeSize.Large
      )
    }
  }

  @Preview(name = "Frozen Status - Small")
  @Composable
  fun StatusBadgeFrozenSmall() {
    AppPreview {
      StatusBadge(
        status = InstanceStatus.Frozen,
        size = StatusBadgeSize.Small
      )
    }
  }

  @Preview(name = "Consumed Status - Small")
  @Composable
  fun StatusBadgeConsumedSmall() {
    AppPreview {
      StatusBadge(
        status = InstanceStatus.Consumed,
        size = StatusBadgeSize.Small
      )
    }
  }
}
