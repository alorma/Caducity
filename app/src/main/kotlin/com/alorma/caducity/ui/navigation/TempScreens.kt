package com.alorma.caducity.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.domain.usecase.ObservePantryUseCase
import org.koin.compose.koinInject

// TEMP(fase 3): placeholders until the real screens land. Each one is deleted by the phase noted below.

/** TEMP(fase 4): replaced by the Add screen. */
@Composable
fun AddPlaceholderScreen(modifier: Modifier = Modifier) = Placeholder("Add", modifier)

/** TEMP(fase 5c): replaced by the Calendar screen. */
@Composable
fun CalendarPlaceholderScreen(modifier: Modifier = Modifier) = Placeholder("Calendar", modifier)

/** TEMP(fase 5a): replaced by the item actions sheet. */
@Composable
fun ItemActionsPlaceholder(itemId: String) = Placeholder("Item actions: $itemId", Modifier.padding(32.dp))

/** TEMP(fase 4): replaced by the quick category sheet. */
@Composable
fun QuickCategoryPlaceholder() = Placeholder("Quick category", Modifier.padding(32.dp))

/** TEMP(fase 6): replaced by the Pantry screen. */
@Composable
fun PantryListPlaceholder(
  onCategoryClick: (String) -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
  observePantry: ObservePantryUseCase = koinInject(),
) {
  val data by observePantry.observe().collectAsStateWithLifecycle(initialValue = null)
  LazyColumn(
    modifier = modifier.fillMaxSize().statusBarsPadding(),
    contentPadding = PaddingValues(horizontal = 20.dp),
  ) {
    item {
      Text(
        text = "Settings",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSettingsClick).padding(vertical = 12.dp),
      )
    }
    items(data?.categories.orEmpty(), key = { it.category.id }) { entry ->
      Text(
        text = "${entry.category.name} · ${entry.unitCount}",
        style = MaterialTheme.typography.titleMedium,
        modifier =
          Modifier
            .fillMaxWidth()
            .clickable { onCategoryClick(entry.category.id) }
            .padding(vertical = 12.dp),
      )
    }
  }
}

@Composable
private fun Placeholder(
  text: String,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
  }
}
