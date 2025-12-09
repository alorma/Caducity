package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_screen_title
import caducity.composeapp.generated.resources.dashboard_section_expired
import caducity.composeapp.generated.resources.dashboard_section_expiring_soon
import caducity.composeapp.generated.resources.dashboard_section_fresh
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues = paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      state.sections.forEach { section ->
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
            ProductItem(product = product)
          }
        }

        item(key = "spacer_${section.type}") {
          Spacer(modifier = Modifier.height(16.dp))
        }
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

      if (product.instances.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(4.dp))

        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          product.instances.forEach { instance ->
            ProductInstanceItem(instance = instance)
          }
        }
      }
    }
  }
}

@Composable
private fun ProductInstanceItem(
  instance: ProductInstanceUiModel,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column {
      Text(
        text = "Expires: ${instance.expirationDate}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "Purchased: ${instance.purchaseDate}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
