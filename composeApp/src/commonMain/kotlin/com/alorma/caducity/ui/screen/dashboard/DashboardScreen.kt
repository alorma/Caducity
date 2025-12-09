package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_screen_title
import caducity.composeapp.generated.resources.dashboard_section_expired
import caducity.composeapp.generated.resources.dashboard_section_expiring_soon
import caducity.composeapp.generated.resources.dashboard_section_fresh
import com.alorma.caducity.ui.adaptive.isWidthCompact
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues = paddingValues),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      DashboardGrid(sections = state.sections)
    }
  }
}

@Composable
private fun DashboardGrid(
  sections: List<DashboardSection>,
) {
  val isCompact = isWidthCompact()
  val columnsPerRow = if (isCompact) 2 else 3

  LazyVerticalGrid(
    columns = GridCells.Fixed(columnsPerRow),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    sections.forEachIndexed { index, section ->
      item {
        DashboardCompactCard(
          sectionType = section.type,
          value = section.itemCount.toString(),
          onClick = { },
          modifier = Modifier,
        )
      }
    }
  }
}

@Composable
private fun DashboardCompactCard(
  sectionType: SectionType,
  value: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val sectionColors = DashboardSectionColors.getSectionColors(sectionType)

  Box(
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .size(160.dp, 180.dp)
        .background(
          color = sectionColors.container,
          shape = MaterialShapes.Pill.toShape(),
        )
        .clip(MaterialShapes.Pill.toShape())
        .then(modifier),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .clickable(onClick = onClick)
          .padding(vertical = 48.dp, horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = when (sectionType) {
            SectionType.EXPIRED -> Res.string.dashboard_section_expired
            SectionType.EXPIRING_SOON -> Res.string.dashboard_section_expiring_soon
            SectionType.FRESH -> Res.string.dashboard_section_fresh
          }.let { stringResource(it) },
          style = MaterialTheme.typography.titleMedium,
        )
        Text(
          text = value,
          style = MaterialTheme.typography.displayMedium,
        )
      }
    }
  }
}
