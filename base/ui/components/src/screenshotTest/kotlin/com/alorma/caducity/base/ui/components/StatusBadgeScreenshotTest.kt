package com.alorma.caducity.base.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.base.ui.theme.preview.AppPreview

class StatusBadgeScreenshotTest {

  @Preview(name = "Fresh Status - Small")
  @Composable
  fun StatusBadgeFreshSmall() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Fresh,
          size = StatusBadgeSize.Small,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Fresh Status - Medium")
  @Composable
  fun StatusBadgeFreshMedium() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Fresh,
          size = StatusBadgeSize.Medium,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Fresh Status - Large")
  @Composable
  fun StatusBadgeFreshLarge() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Fresh,
          size = StatusBadgeSize.Large,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Expiring Soon Status - Small")
  @Composable
  fun StatusBadgeExpiringSoonSmall() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.ExpiringSoon,
          size = StatusBadgeSize.Small,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Expiring Soon Status - Medium")
  @Composable
  fun StatusBadgeExpiringSoonMedium() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.ExpiringSoon,
          size = StatusBadgeSize.Medium,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Expiring Soon Status - Large")
  @Composable
  fun StatusBadgeExpiringSoonLarge() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.ExpiringSoon,
          size = StatusBadgeSize.Large,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Expired Status - Small")
  @Composable
  fun StatusBadgeExpiredSmall() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Expired,
          size = StatusBadgeSize.Small,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Expired Status - Medium")
  @Composable
  fun StatusBadgeExpiredMedium() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Expired,
          size = StatusBadgeSize.Medium,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Expired Status - Large")
  @Composable
  fun StatusBadgeExpiredLarge() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Expired,
          size = StatusBadgeSize.Large,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Frozen Status - Small")
  @Composable
  fun StatusBadgeFrozenSmall() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Frozen,
          size = StatusBadgeSize.Small,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Frozen Status - Medium")
  @Composable
  fun StatusBadgeFrozenMedium() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Frozen,
          size = StatusBadgeSize.Medium,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Frozen Status - Large")
  @Composable
  fun StatusBadgeFrozenLarge() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Frozen,
          size = StatusBadgeSize.Large,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Consumed Status - Small")
  @Composable
  fun StatusBadgeConsumedSmall() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Consumed,
          size = StatusBadgeSize.Small,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Consumed Status - Medium")
  @Composable
  fun StatusBadgeConsumedMedium() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Consumed,
          size = StatusBadgeSize.Medium,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Consumed Status - Large")
  @Composable
  fun StatusBadgeConsumedLarge() {
    AppPreview {
      Surface {
        StatusBadge(
          status = InstanceStatus.Consumed,
          size = StatusBadgeSize.Large,
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "All Statuses - Small Size Comparison")
  @Composable
  fun AllStatusesSmallComparison() {
    AppPreview {
      Surface {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatusBadge(status = InstanceStatus.Fresh, size = StatusBadgeSize.Small)
          StatusBadge(status = InstanceStatus.ExpiringSoon, size = StatusBadgeSize.Small)
          StatusBadge(status = InstanceStatus.Expired, size = StatusBadgeSize.Small)
          StatusBadge(status = InstanceStatus.Frozen, size = StatusBadgeSize.Small)
          StatusBadge(status = InstanceStatus.Consumed, size = StatusBadgeSize.Small)
        }
      }
    }
  }

  @Preview(name = "All Statuses - Medium Size Comparison")
  @Composable
  fun AllStatusesMediumComparison() {
    AppPreview {
      Surface {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatusBadge(status = InstanceStatus.Fresh, size = StatusBadgeSize.Medium)
          StatusBadge(status = InstanceStatus.ExpiringSoon, size = StatusBadgeSize.Medium)
          StatusBadge(status = InstanceStatus.Expired, size = StatusBadgeSize.Medium)
          StatusBadge(status = InstanceStatus.Frozen, size = StatusBadgeSize.Medium)
          StatusBadge(status = InstanceStatus.Consumed, size = StatusBadgeSize.Medium)
        }
      }
    }
  }

  @Preview(name = "All Statuses - Large Size Comparison")
  @Composable
  fun AllStatusesLargeComparison() {
    AppPreview {
      Surface {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatusBadge(status = InstanceStatus.Fresh, size = StatusBadgeSize.Large)
          StatusBadge(status = InstanceStatus.ExpiringSoon, size = StatusBadgeSize.Large)
          StatusBadge(status = InstanceStatus.Expired, size = StatusBadgeSize.Large)
          StatusBadge(status = InstanceStatus.Frozen, size = StatusBadgeSize.Large)
          StatusBadge(status = InstanceStatus.Consumed, size = StatusBadgeSize.Large)
        }
      }
    }
  }
}
