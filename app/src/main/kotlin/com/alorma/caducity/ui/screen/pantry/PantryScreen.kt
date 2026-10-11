package com.alorma.caducity.ui.screen.pantry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryCategory
import com.alorma.caducity.domain.model.PantryData
import com.alorma.caducity.feature.tracking.PantryScreen as PantryScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.product.ProductGroupRow
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.navigation.LocalFloatingBarPadding
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PantryScreen(
  onOpenCategory: (categoryId: String) -> Unit,
  onOpenNewCategory: () -> Unit,
  onOpenItemActions: (itemId: String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PantryViewModel = koinViewModel(),
  searchRequests: PantrySearchRequests = koinInject(),
) {
  TrackScreen(screen = PantryScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()
  val focusRequester = remember { FocusRequester() }
  val keyboard = LocalSoftwareKeyboardController.current

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        is PantryNavigationSideEffect.OpenCategory -> onOpenCategory(effect.categoryId)
        PantryNavigationSideEffect.OpenNewCategory -> onOpenNewCategory()
      }
    }
  }
  LaunchedEffect(searchRequests) {
    searchRequests.requests.collect {
      focusRequester.requestFocus()
      keyboard?.show()
    }
  }

  ResponsiveCenteredContainer(modifier = modifier) {
    when (val current = state) {
      PantryState.Loading -> Unit
      is PantryState.Ready ->
        PantryContent(
          state = current,
          focusRequester = focusRequester,
          onQueryChange = viewModel::onQueryChange,
          onNavigate = viewModel::navigate,
          onOpenItemActions = onOpenItemActions,
        )
    }
  }
}

@Composable
internal fun PantryContent(
  state: PantryState.Ready,
  focusRequester: FocusRequester,
  onQueryChange: (String) -> Unit,
  onNavigate: (PantryNavigation) -> Unit,
  onOpenItemActions: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val data = state.data
  var expanded by rememberSaveable { mutableStateOf(emptySet<String>()) }
  val columns = 2

  Column(modifier = modifier.fillMaxSize()) {
    PantryHeader(data)
    Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp)) {
      PantrySearchField(state.query, onQueryChange, focusRequester)
    }
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding =
        PaddingValues(
          start = 20.dp,
          end = 20.dp,
          bottom = LocalFloatingBarPadding.current.calculateBottomPadding() + 24.dp,
        ),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      if (state.isSearching) {
        searchResults(state, expanded, { key ->
          expanded =
            if (key in
              expanded
            ) {
              expanded - key
            } else {
              expanded + key
            }
        }, onOpenItemActions)
      } else {
        val cells: List<PantryCategory?> = data.categories + null
        items(cells.chunked(columns), key = { row -> row.first()?.category?.id ?: "new-category" }) { row ->
          Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            row.forEach { cell ->
              if (cell == null) {
                NewCategoryCard(
                  onClick = { onNavigate(PantryNavigation.NewCategory) },
                  modifier = Modifier.weight(1f).fillMaxHeight(),
                )
              } else {
                CategoryCard(
                  entry = cell,
                  today = data.today,
                  onClick = { onNavigate(PantryNavigation.Category(cell.category.id, "card")) },
                  modifier = Modifier.weight(1f).fillMaxHeight(),
                )
              }
            }
            if (row.size < columns) {
              repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
          }
        }
      }
    }
  }
}

private fun <T> LazyListScope.items(
  list: List<T>,
  key: (T) -> Any,
  content: @Composable (T) -> Unit,
) = itemsIndexed(list, key = { _, item -> key(item) }) { _, item -> content(item) }

private fun LazyListScope.searchResults(
  state: PantryState.Ready,
  expanded: Set<String>,
  onToggle: (String) -> Unit,
  onOpenItemActions: (String) -> Unit,
) {
  if (state.results.isEmpty()) {
    item(key = "no-results") {
      Text(
        text = stringResource(R.string.pantry_no_results),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 24.dp),
      )
    }
    return
  }
  itemsIndexed(state.results, key = { _, result -> "result-${result.group.key}" }) { index, result ->
    ProductGroupRow(
      group = result.group,
      today = state.data.today,
      index = index,
      count = state.results.size,
      expanded = result.group.key in expanded,
      onToggleExpanded = { onToggle(result.group.key) },
      onUnitClick = onOpenItemActions,
      categoryName = result.category.name,
    )
  }
}

@Composable
private fun PantryHeader(data: PantryData) {
  StyledTopAppBar(
    title = {
      Column {
        Text(
          text =
            stringResource(
              R.string.pantry_overline,
              pluralStringResource(R.plurals.home_product_count, data.totalUnits, data.totalUnits),
              pluralStringResource(R.plurals.pantry_category_count, data.categories.size, data.categories.size),
            ).uppercase(),
          style = CaducityTheme.typography.labelSmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = stringResource(R.string.nav_pantry),
          style = CaducityTheme.typography.headlineMedium,
          modifier = Modifier.semantics { heading() },
        )
      }
    },
  )
}

// region Previews

private val previewToday = LocalDate(2026, 10, 11)

private fun previewCategory(
  id: String,
  name: String,
  shape: CategoryShape,
  icon: String,
  units: Int,
  expired: Int,
) = PantryCategory(
  category = Category(id, name, "", shape, CategoryIcon(icon)),
  unitCount = units,
  expiredCount = expired,
  statusCounts =
    buildMap {
      if (expired > 0) put(ExpiryStatus.EXPIRED, expired)
      put(ExpiryStatus.SOON, 2)
      put(ExpiryStatus.FRESH, units - expired - 2)
    },
  nearest = previewToday,
)

@PreviewLightDark
@Composable
private fun PantryPreview() {
  PreviewTheme {
    PantryContent(
      state =
        PantryState.Ready(
          data =
            PantryData(
              today = previewToday,
              totalUnits = 61,
              categories =
                listOf(
                  previewCategory("dairy", "Làctics", CategoryShape.COOKIE, "water_full", 14, 1),
                  previewCategory("produce", "Fruita i verdura", CategoryShape.CLOVER8, "nutrition", 20, 0),
                  previewCategory("meat", "Carn i peix", CategoryShape.CLOVER, "set_meal", 9, 3),
                ),
            ),
          query = "",
          results = emptyList(),
        ),
      focusRequester = remember { FocusRequester() },
      onQueryChange = {},
      onNavigate = {},
      onOpenItemActions = {},
    )
  }
}

// endregion
