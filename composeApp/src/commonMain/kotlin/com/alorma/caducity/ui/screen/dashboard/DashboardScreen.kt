package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_screen_title
import caducity.composeapp.generated.resources.dashboard_section_empty
import caducity.composeapp.generated.resources.dashboard_section_expired
import caducity.composeapp.generated.resources.dashboard_section_expiring_soon
import caducity.composeapp.generated.resources.dashboard_section_fresh
import com.alorma.caducity.ui.adaptive.isWidthCompact
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel = koinViewModel()
) {
  val dashboardState = viewModel.state.collectAsStateWithLifecycle()

  when (val state = dashboardState.value) {
    is DashboardState.Loading -> {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        LoadingIndicator(
          color = MaterialTheme.colorScheme.secondary,
          polygons = listOf(
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Cookie6Sided,
          ),
        )
      }
    }

    is DashboardState.Success -> DashboardContent(state)
  }
}

@Composable
fun DashboardContent(state: DashboardState.Success) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(Res.string.dashboard_screen_title)) },
      )
    },
  ) { paddingValues ->
    val isCompact = isWidthCompact()
    if (isCompact) {
      DashboardCompactLayout(
        sections = state.sections,
        paddingValues = paddingValues,
      )
    } else {
      DashboardExpandedLayout(
        sections = state.sections,
        paddingValues = paddingValues,
      )
    }
  }
}

@Composable
private fun DashboardCompactLayout(
  sections: List<DashboardSection>,
  paddingValues: PaddingValues,
) {
  val pagerState = rememberPagerState(pageCount = { sections.size })

  VerticalPager(
    state = pagerState,
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues),
    contentPadding = PaddingValues(horizontal = 16.dp),
    pageSpacing = 16.dp,
  ) { page ->
    SectionColumn(section = sections[page])
  }
}

@Composable
private fun DashboardExpandedLayout(
  sections: List<DashboardSection>,
  paddingValues: PaddingValues,
) {
  Row(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    sections.forEach { section ->
      Box(modifier = Modifier.weight(1f)) {
        SectionColumn(section = section)
      }
    }
  }
}

@Composable
private fun SectionColumn(section: DashboardSection) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    item(key = "header_${section.type}") {
      DashboardSectionHeader(section = section)
    }

    if (section.products.isEmpty()) {
      item(key = "empty_${section.type}") {
        Text(
          text = "No products in this section",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }
    } else {
      items(
        items = section.products,
        key = { product -> product.id }
      ) { product ->
        ProductItem(product = product, sectionType = section.type)
      }
    }
  }
}

@Composable
private fun DashboardSectionHeader(
  section: DashboardSection,
) {
  val sectionColors = DashboardSectionColors.getSectionColors(section.type)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        color = sectionColors.container,
        shape = RoundedCornerShape(12.dp),
      )
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = when (section.type) {
        SectionType.EXPIRED -> stringResource(Res.string.dashboard_section_expired)
        SectionType.EXPIRING_SOON -> stringResource(Res.string.dashboard_section_expiring_soon)
        SectionType.FRESH -> stringResource(Res.string.dashboard_section_fresh)
        SectionType.EMPTY -> stringResource(Res.string.dashboard_section_empty)
      },
      style = MaterialTheme.typography.titleLarge,
      color = sectionColors.onContainer,
    )
    Text(
      text = section.itemCount.toString(),
      style = MaterialTheme.typography.titleLarge,
      color = sectionColors.onContainer,
    )
  }
}

@Composable
private fun ProductItem(
  product: ProductUiModel,
  sectionType: SectionType,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    shape = RoundedCornerShape(12.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = product.name,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      if (product.description.isNotEmpty()) {
        Text(
          text = product.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      when (product) {
        is ProductUiModel.WithInstances -> {
          val firstVisibleDate = if (product.instances.any { it.expirationDate <= product.today }) {
            // If there are items expiring on or before today, show today's week
            product.today
          } else {
            // Otherwise, show the week with the nearest (earliest) upcoming item
            product.startDate
          }

          val state = rememberWeekCalendarState(
            startDate = product.startDate,
            endDate = product.endDate,
            firstVisibleWeekDate = firstVisibleDate,
          )

          WeekCalendar(
            state = state,
            dayContent = { weekDay ->
              val hasItem = weekDay.date in product.instances.map { it.expirationDate }
              val isToday = weekDay.date == product.today
              val backgroundColor = when {
                isToday && hasItem -> MaterialTheme.colorScheme.primary
                isToday && !hasItem -> Color.Unspecified
                hasItem -> DashboardSectionColors.getSectionColors(sectionType).container
                else -> Color.Unspecified
              }
              val contentColor = when {
                isToday && hasItem -> MaterialTheme.colorScheme.onPrimary
                isToday && !hasItem -> MaterialTheme.colorScheme.primary
                hasItem -> DashboardSectionColors.getSectionColors(sectionType).onContainer
                else -> Color.Unspecified
              }
              Column(
                modifier = Modifier
                  .widthIn(56.dp)
                  .clip(
                    when {
                      isToday && hasItem -> MaterialShapes.Cookie6Sided.toShape()
                      isToday && !hasItem -> RectangleShape
                      hasItem -> MaterialShapes.Cookie4Sided.toShape()
                      else -> RectangleShape
                    }
                  )
                  .background(backgroundColor)
                  .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  text = weekDay.date.day.toString(),
                  style = MaterialTheme.typography.labelMediumEmphasized,
                  color = contentColor,
                )
                Text(
                  text = weekDay.date.dayOfWeek.toString().take(3),
                  style = MaterialTheme.typography.labelSmall,
                  color = contentColor,
                )
              }
            }
          )
        }

        is ProductUiModel.Empty -> {
          Text(
            text = "No active instances",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
