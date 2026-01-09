package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.calendar.CaducityWeekCalendar
import com.alorma.caducity.ui.screen.dashboard.DashboardState
import com.alorma.caducity.ui.theme.CaducityTheme

@Composable
fun DashboardPerProduct(
  state: DashboardState.Success.PerProduct,
  onNavigateToProduct: (String) -> Unit
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(24.dp),
    contentPadding = PaddingValues(
      start = 16.dp,
      end = 16.dp,
      bottom = 64.dp,
    ),
  ) {
    items(
      items = state.products,
      key = { product -> product.id },
      contentType = { "product" },
    ) { productCalendarState ->
      Text(
        text = productCalendarState.name,
        style = CaducityTheme.typography.titleMedium,
      )

      CaducityWeekCalendar(
        calendarState = productCalendarState.calendarState,
        onDateClick = { onNavigateToProduct(productCalendarState.id) },
      )
    }
  }
}