package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.screen.dashboard.DashboardSummary

class DashboardSummaryCardScreenshotTest {

  @Preview(name = "Dashboard Summary - Balanced Mix")
  @Composable
  fun DashboardSummaryBalanced() {
    AppPreview {
      Surface {
        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 3,
            expiringSoon = 5,
            fresh = 12,
            frozen = 2
          ),
          onStatusClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Dashboard Summary - All Zeros")
  @Composable
  fun DashboardSummaryEmpty() {
    AppPreview {
      Surface {
        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 0,
            expiringSoon = 0,
            fresh = 0,
            frozen = 0
          ),
          onStatusClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Dashboard Summary - High Numbers")
  @Composable
  fun DashboardSummaryHighNumbers() {
    AppPreview {
      Surface {
        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 24,
            expiringSoon = 18,
            fresh = 156,
            frozen = 42
          ),
          onStatusClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Dashboard Summary - Mostly Fresh")
  @Composable
  fun DashboardSummaryMostlyFresh() {
    AppPreview {
      Surface {
        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 0,
            expiringSoon = 2,
            fresh = 45,
            frozen = 8
          ),
          onStatusClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Dashboard Summary - Urgent Attention Needed")
  @Composable
  fun DashboardSummaryUrgent() {
    AppPreview {
      Surface {
        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 15,
            expiringSoon = 23,
            fresh = 5,
            frozen = 1
          ),
          onStatusClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Dashboard Summary - Only Frozen")
  @Composable
  fun DashboardSummaryOnlyFrozen() {
    AppPreview {
      Surface {
        DashboardSummaryCard(
          summary = DashboardSummary(
            expired = 0,
            expiringSoon = 0,
            fresh = 0,
            frozen = 28
          ),
          onStatusClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }
}
