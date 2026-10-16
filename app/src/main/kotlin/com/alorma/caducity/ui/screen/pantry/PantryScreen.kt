package com.alorma.caducity.ui.screen.pantry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.alorma.caducity.domain.model.ProductRowDetail
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.domain.model.toProductRow
import com.alorma.caducity.feature.tracking.PantryScreen as PantryScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.product.ProductRow
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.StyledLargeTopAppBar
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
  onOpenItemActions: (itemIds: List<String>) -> Unit,
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

  when (val current = state) {
    PantryState.Loading -> Unit
    is PantryState.Ready ->
      PantryContent(
        state = current,
        focusRequester = focusRequester,
        onQueryChange = viewModel::onQueryChange,
        onNavigate = viewModel::navigate,
        onOpenItemActions = onOpenItemActions,
        onConsume = viewModel::onConsume,
        onFreeze = viewModel::onFreeze,
        onUnfreeze = viewModel::onUnfreeze,
        onDiscard = viewModel::onDiscard,
        modifier = modifier,
      )
  }
}

@Composable
internal fun PantryContent(
  state: PantryState.Ready,
  focusRequester: FocusRequester,
  onQueryChange: (String) -> Unit,
  onNavigate: (PantryNavigation) -> Unit,
  onOpenItemActions: (List<String>) -> Unit,
  onConsume: (ProductRowModel) -> Unit,
  onFreeze: (ProductRowModel) -> Unit,
  onUnfreeze: (ProductRowModel) -> Unit,
  onDiscard: (ProductRowModel) -> Unit,
  modifier: Modifier = Modifier,
) {
  val data = state.data
  val columns = 2

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  // The floating bar overlays the content, so the scaffold reserves no space at the bottom.
  AppScaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    contentWindowInsets = WindowInsets(0.dp),
    topBar = {
      ResponsiveCenteredContainer(fillHeight = false) { PantryHeader(data, scrollBehavior) }
    },
  ) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 10.dp)) {
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
            searchResults(state, onOpenItemActions, onConsume, onFreeze, onUnfreeze, onDiscard)
          } else {
            val cells: List<PantryCategory?> = data.categories + null
            items(
              cells.chunked(columns),
              key = { row -> row.first()?.category?.id ?: "new-category" },
              contentType = "category-row",
            ) { row ->
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
  }
}

private fun <T> LazyListScope.items(
  list: List<T>,
  key: (T) -> Any,
  contentType: Any,
  content: @Composable (T) -> Unit,
) = itemsIndexed(
  list,
  key = { _, item -> key(item) },
  contentType = { _, _ -> contentType },
) { _, item -> content(item) }

private fun LazyListScope.searchResults(
  state: PantryState.Ready,
  onOpenItemActions: (List<String>) -> Unit,
  onConsume: (ProductRowModel) -> Unit,
  onFreeze: (ProductRowModel) -> Unit,
  onUnfreeze: (ProductRowModel) -> Unit,
  onDiscard: (ProductRowModel) -> Unit,
) {
  if (state.results.isEmpty()) {
    item(key = "no-results", contentType = "no-results") {
      Text(
        text = stringResource(R.string.pantry_no_results),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 24.dp),
      )
    }
    return
  }
  itemsIndexed(
    state.results,
    key = { _, result -> "result-${result.group.key}" },
    contentType = { _, _ -> "result" },
  ) { index, result ->
    val row =
      result.group.units.toProductRow(
        category = result.category,
        detail = ProductRowDetail.WithDays,
        today = state.data.today,
      )
    ProductRow(
      model = row,
      index = index,
      count = state.results.size,
      onClick = { onOpenItemActions(row.itemIds) },
      onConsume = { onConsume(row) },
      onFreeze = { onFreeze(row) },
      onUnfreeze = { onUnfreeze(row) },
      onDiscard = { onDiscard(row) },
    )
  }
}

@Composable
private fun PantryHeader(
  data: PantryData,
  scrollBehavior: TopAppBarScrollBehavior,
) {
  StyledLargeTopAppBar(
    scrollBehavior = scrollBehavior,
    title = { Text(text = stringResource(R.string.nav_pantry), modifier = Modifier.semantics { heading() }) },
    subtitle = {
      Text(
        text =
          stringResource(
            R.string.pantry_overline,
            pluralStringResource(R.plurals.home_product_count, data.totalProducts, data.totalProducts),
            pluralStringResource(R.plurals.pantry_category_count, data.categories.size, data.categories.size),
          ),
      )
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
  productCount = units,
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
  PreviewTheme { PantryPreviewContent() }
}

/** Rebost amb dades d'exemple, sense tema (el posa qui el crida). */
@Composable
internal fun PantryPreviewContent() {
  PantryContent(
    state =
      PantryState.Ready(
        data =
          PantryData(
            today = previewToday,
            totalProducts = 61,
            categories =
              listOf(
                previewCategory("dairy", "Làctics", CategoryShape.COOKIE, "water_full", 14, 1),
                previewCategory("produce", "Fruita i verdura", CategoryShape.COOKIE7, "nutrition", 20, 0),
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
    onConsume = {},
    onFreeze = {},
    onUnfreeze = {},
    onDiscard = {},
  )
}

// endregion
