package com.alorma.caducity.ui.screen.transform.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Search
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductGroup
import com.alorma.caducity.ui.components.chips.FilterChipOption
import com.alorma.caducity.ui.components.chips.FilterChips
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.connectedShape
import com.alorma.caducity.ui.components.status.StatusPill
import com.alorma.caducity.ui.screen.add.ScreenGutter
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Callbacks of the picker, grouped so the content can be previewed without a view model. */
class IngredientPickerActions(
  val onClose: () -> Unit,
  val onConfirm: () -> Unit,
  val onQueryChange: (String) -> Unit,
  val onStatusFilterSelect: (PickerStatusFilter) -> Unit,
  val onCategoryFilterSelect: (String) -> Unit,
  val onToggle: (String) -> Unit,
)

@Composable
fun IngredientPickerScreen(
  args: IngredientPickerArgs,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: IngredientPickerViewModel = koinViewModel { parametersOf(args) },
) {
  val state by viewModel.state.collectAsState()
  val actions =
    IngredientPickerActions(
      onClose = { viewModel.navigate(IngredientPickerNavigation.Close) },
      onConfirm = { viewModel.navigate(IngredientPickerNavigation.Confirm) },
      onQueryChange = viewModel::onQueryChange,
      onStatusFilterSelect = viewModel::onStatusFilterSelect,
      onCategoryFilterSelect = viewModel::onCategoryFilterSelect,
      onToggle = viewModel::onToggle,
    )

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        IngredientPickerNavigationSideEffect.Close -> onClose()
      }
    }
  }

  IngredientPickerContent(state = state, actions = actions, modifier = modifier)
}

@Composable
internal fun IngredientPickerContent(
  state: IngredientPickerState,
  actions: IngredientPickerActions,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = modifier,
    topBar = { PickerTopBar(onClose = actions.onClose) },
    bottomBar = {
      PickerBottomBar(count = state.selectedCount, onConfirm = actions.onConfirm)
    },
  ) { insets ->
    ResponsiveCenteredContainer {
      Column(
        modifier = Modifier.fillMaxSize().padding(insets).padding(horizontal = ScreenGutter),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        SearchField(query = state.query, onQueryChange = actions.onQueryChange)
        StatusChips(selected = state.statusFilter, onSelect = actions.onStatusFilterSelect)
        CategoryChips(
          categories = state.categories,
          selectedId = state.categoryFilter,
          onSelect = actions.onCategoryFilterSelect,
        )
        PickerList(state = state, onToggle = actions.onToggle)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerTopBar(onClose: () -> Unit) {
  ResponsiveCenteredContainer(fillHeight = false) {
    TopAppBar(
      title = {
        Text(text = stringResource(R.string.transform_picker_title), style = CaducityTheme.typography.titleLarge)
      },
      navigationIcon = {
        IconButton(onClick = onClose) {
          Icon(Symbols.Outlined.Close, contentDescription = stringResource(R.string.add_close))
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
  }
}

@Composable
private fun SearchField(
  query: String,
  onQueryChange: (String) -> Unit,
) {
  TextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
    singleLine = true,
    shape = CaducityShapes.full,
    placeholder = { Text(stringResource(R.string.transform_picker_search)) },
    leadingIcon = { Icon(Symbols.Outlined.Search, contentDescription = null) },
    trailingIcon =
      if (query.isNotEmpty()) {
        {
          IconButton(onClick = { onQueryChange("") }) {
            Icon(Symbols.Outlined.Close, contentDescription = stringResource(R.string.pantry_search_clear))
          }
        }
      } else {
        null
      },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    colors =
      TextFieldDefaults.colors(
        focusedContainerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
        unfocusedContainerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
      ),
  )
}

@Composable
private fun StatusChips(
  selected: PickerStatusFilter,
  onSelect: (PickerStatusFilter) -> Unit,
) {
  val options =
    PickerStatusFilter.entries.map { filter ->
      FilterChipOption(label = filter.label(), status = filter.status)
    }
  FilterChips(
    options = options,
    selectedIndex = selected.ordinal,
    onSelect = { onSelect(PickerStatusFilter.entries[it]) },
  )
}

@Composable
private fun CategoryChips(
  categories: List<Category>,
  selectedId: String?,
  onSelect: (String) -> Unit,
) {
  if (categories.isEmpty()) return
  FilterChips(
    options = categories.map { FilterChipOption(label = it.name) },
    selectedIndex = categories.indexOfFirst { it.id == selectedId }.takeIf { it >= 0 },
    onSelect = { onSelect(categories[it].id) },
  )
}

@Composable
private fun PickerStatusFilter.label(): String =
  when (this) {
    PickerStatusFilter.All -> stringResource(R.string.transform_picker_filter_all)
    PickerStatusFilter.Soon -> stringResource(R.string.status_title_soon)
    PickerStatusFilter.Fresh -> stringResource(R.string.status_title_fresh)
    PickerStatusFilter.Frozen -> stringResource(R.string.status_title_frozen)
  }

@Composable
private fun PickerList(
  state: IngredientPickerState,
  onToggle: (String) -> Unit,
) {
  val groups = state.visibleGroups
  if (groups.isEmpty()) {
    Text(
      text = stringResource(R.string.transform_picker_empty),
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(vertical = 24.dp, horizontal = 4.dp),
    )
    return
  }
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
    contentPadding = PaddingValues(bottom = 16.dp),
  ) {
    items(groups, key = { it.key }) { group ->
      val index = groups.indexOf(group)
      PickerRow(
        group = group,
        category = state.categories.firstOrNull { it.id == group.categoryId },
        selected = group.key in state.selectedKeys,
        index = index,
        count = groups.size,
        onToggle = { onToggle(group.key) },
      )
    }
  }
}

@Composable
private fun PickerRow(
  group: ProductGroup,
  category: Category?,
  selected: Boolean,
  index: Int,
  count: Int,
  onToggle: () -> Unit,
) {
  val status = group.nearest.status
  val units = pluralStringResource(R.plurals.home_unit_count, group.unitCount, group.unitCount)
  Surface(
    shape = connectedShape(index, count),
    color =
      if (selected) CaducityTheme.colorScheme.primaryContainer else CaducityTheme.colorScheme.surfaceContainer,
    contentColor =
      if (selected) CaducityTheme.colorScheme.onPrimaryContainer else CaducityTheme.colorScheme.onSurface,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier =
        Modifier
          .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
          .padding(horizontal = 12.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(checked = selected, onCheckedChange = null)
      Column(
        modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = group.name,
          style = CaducityTheme.typography.titleSmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text =
            category?.let { stringResource(R.string.transform_picker_meta, it.name, units) } ?: units,
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      StatusPill(status = status, label = status.label())
    }
  }
}

@Composable
private fun ExpiryStatus.label(): String =
  when (this) {
    ExpiryStatus.EXPIRED -> stringResource(R.string.status_title_expired)
    ExpiryStatus.SOON -> stringResource(R.string.status_title_soon)
    ExpiryStatus.FRESH -> stringResource(R.string.status_title_fresh)
    ExpiryStatus.FROZEN -> stringResource(R.string.status_title_frozen)
  }

@Composable
private fun PickerBottomBar(
  count: Int,
  onConfirm: () -> Unit,
) {
  ResponsiveCenteredContainer(fillHeight = false) {
    Button(
      onClick = onConfirm,
      enabled = count > 0,
      modifier =
        Modifier
          .navigationBarsPadding()
          .fillMaxWidth()
          .padding(horizontal = ScreenGutter, vertical = 12.dp)
          .heightIn(min = 56.dp),
    ) {
      Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(
        text = pluralStringResource(R.plurals.transform_picker_add, count, count),
        style = CaducityTheme.typography.labelLarge,
        modifier = Modifier.padding(start = 10.dp),
      )
    }
  }
}
