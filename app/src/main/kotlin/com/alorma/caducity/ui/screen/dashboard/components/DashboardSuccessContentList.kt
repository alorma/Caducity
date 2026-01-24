package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.calendar.CaducityWeekCalendar
import com.alorma.caducity.ui.screen.dashboard.DashboardState
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.datetime.LocalDate

@Composable
fun DashboardSuccessContentList(
  state: DashboardState.Success,
  onNavigateToProduct: (String) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  lazyListState: LazyListState,
) {
  LazyColumn(
    state = lazyListState,
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(
      start = 16.dp,
      end = 16.dp,
      bottom = 80.dp,
    ),
  ) {
    item(
      key = "summary",
      contentType = "summary"
    ) {
      DashboardSummaryCard(
        modifier = Modifier.animateItem(),
        summary = state.summary,
        onStatusClick = { status -> onNavigateToStatus(status) },
      )
    }

    when (state) {
      is DashboardState.Success.PerProduct -> {
        items(
          items = state.products,
          key = { product -> product.id },
          contentType = { "product" },
        ) { productCalendarState ->
          Column(
            modifier = Modifier.animateItem(),
          ) {
            Text(
              text = productCalendarState.name,
              style = CaducityTheme.typography.titleMedium,
            )

            CaducityWeekCalendar(
              appCalendarConfig = productCalendarState.appCalendarConfig,
              todayColor = CaducityTheme.colorScheme.surfaceContainerHighest,
              onDateClick = { onNavigateToProduct(productCalendarState.id) },
            )
          }

        }
      }
    }
  }
}