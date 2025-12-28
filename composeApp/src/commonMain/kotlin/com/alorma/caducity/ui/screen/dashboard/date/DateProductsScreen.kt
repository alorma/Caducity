package com.alorma.caducity.ui.screen.dashboard.date

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.ui.screen.dashboard.DashboardState
import com.alorma.caducity.ui.screen.dashboard.DashboardViewModel
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import com.alorma.caducity.ui.screen.dashboard.components.ProductItem
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateProductsScreen(
  date: LocalDate,
  onDismiss: () -> Unit,
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DashboardViewModel = koinViewModel { parametersOf(false) },
) {
  val dashboardState = viewModel.state.collectAsStateWithLifecycle()

  val productsForDate by remember(dashboardState.value, date) {
    derivedStateOf {
      when (val state = dashboardState.value) {
        is DashboardState.Success -> {
          state.items.filter { product ->
            if (product is ProductUiModel.WithInstances) {
              product.instances.any { instance ->
                instance.expirationDate == date
              }
            } else {
              false
            }
          }
        }

        else -> emptyList()
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Products expiring on ${date}",
      style = MaterialTheme.typography.titleLarge,
      color = CaducityTheme.colorScheme.onSurface,
    )

    LazyColumn(
      contentPadding = PaddingValues(bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(productsForDate, key = { it.id }) { product ->
        ProductItem(
          product = product,
          onClick = { onNavigateToProductDetail(product.id) },
        )
      }

      if (productsForDate.isEmpty()) {
        item {
          Text(
            text = "No products expiring on this date",
            style = MaterialTheme.typography.bodyMedium,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 32.dp),
          )
        }
      }
    }
  }
}
