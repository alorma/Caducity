package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.outlined.CircleChevronRight
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.adaptive.rememberIsExpanded
import com.alorma.caducity.ui.components.calendar.CaducityMonthCalendar
import com.alorma.caducity.ui.components.calendar.CaducityWeekCalendar
import com.alorma.caducity.ui.screen.dashboard.DashboardState
import com.alorma.caducity.ui.theme.CaducityTheme

@Composable
fun DashboardSuccessContentList(
  state: DashboardState.Success,
  onNavigateToCategory: (String) -> Unit,
  onNavigateToStatus: (ItemStatus) -> Unit,
  lazyListState: LazyListState,
) {
  val isExpanded = rememberIsExpanded()

  if (isExpanded) {
    DashboardExpandedLayout(
      state = state,
      onNavigateToCategory = onNavigateToCategory,
      onNavigateToStatus = onNavigateToStatus,
    )
  } else {
    DashboardCompactLayout(
      state = state,
      lazyListState = lazyListState,
      onNavigateToCategory = onNavigateToCategory,
      onNavigateToStatus = onNavigateToStatus,
    )
  }
}

@Composable
private fun DashboardCompactLayout(
  state: DashboardState.Success,
  lazyListState: LazyListState,
  onNavigateToCategory: (String) -> Unit,
  onNavigateToStatus: (ItemStatus) -> Unit,
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
      is DashboardState.Success.PerCategory -> {
        items(
          items = state.categories,
          key = { category -> category.id },
          contentType = { "category" },
        ) { categoryCalendarState ->
          Column(
            modifier = Modifier.animateItem(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(CaducityTheme.shapes.small)
                .clickable {
                  onNavigateToCategory(categoryCalendarState.id)
                }
                .padding(horizontal = 4.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                modifier = Modifier.padding(start = 8.dp).weight(1f),
                text = categoryCalendarState.name,
                style = CaducityTheme.typography.titleMedium,
              )

              Icon(
                modifier = Modifier.padding(end = 8.dp).size(18.dp),
                imageVector = AppIcons.Outlined.CircleChevronRight,
                contentDescription = null,
              )
            }

            CaducityWeekCalendar(
              appCalendarConfig = categoryCalendarState.appCalendarConfig,
              todayColor = CaducityTheme.colorScheme.surfaceContainerHighest,
              onDateClick = { onNavigateToCategory(categoryCalendarState.id) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DashboardExpandedLayout(
  state: DashboardState.Success,
  onNavigateToCategory: (String) -> Unit,
  onNavigateToStatus: (ItemStatus) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    DashboardSummaryCard(
      modifier = Modifier.padding(horizontal = 16.dp),
      summary = state.summary,
      onStatusClick = { status -> onNavigateToStatus(status) },
    )

    when (state) {
      is DashboardState.Success.PerCategory -> {
        LazyRow(
          modifier = Modifier.fillMaxHeight(),
          horizontalArrangement = Arrangement.spacedBy(64.dp),
          contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
          ),
        ) {
          items(
            items = state.categories,
            key = { category -> category.id },
            contentType = { "category" },
          ) { categoryCalendarState ->
            Column(
              modifier = Modifier
                .width(340.dp)
                .fillMaxHeight()
                .animateItem(),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(CaducityTheme.shapes.small)
                  .clickable {
                    onNavigateToCategory(categoryCalendarState.id)
                  }
                  .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  modifier = Modifier.padding(start = 8.dp).weight(1f),
                  text = categoryCalendarState.name,
                  style = CaducityTheme.typography.titleMedium,
                )

                Icon(
                  modifier = Modifier.padding(end = 8.dp).size(18.dp),
                  imageVector = AppIcons.Outlined.CircleChevronRight,
                  contentDescription = null,
                )
              }

              CaducityMonthCalendar(
                appCalendarConfig = categoryCalendarState.appCalendarConfig,
                onDateClick = { onNavigateToCategory(categoryCalendarState.id) },
              )
            }
          }
        }
      }
    }
  }
}
