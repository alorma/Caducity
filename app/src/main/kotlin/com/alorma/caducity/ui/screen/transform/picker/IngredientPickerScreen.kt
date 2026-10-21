package com.alorma.caducity.ui.screen.transform.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.ExpandLess
import com.alorma.caducity.base.ui.icons.symbols.ExpandMore
import com.alorma.caducity.base.ui.icons.symbols.Info
import com.alorma.caducity.base.ui.icons.symbols.Search
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductGroup
import com.alorma.caducity.domain.model.sumAmount
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.status.StatusPill
import com.alorma.caducity.ui.screen.add.ScreenGutter
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import com.alorma.caducity.ui.unit.unitCountText
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Callbacks of the picker, grouped so the content can be previewed without a view model. */
class IngredientPickerActions(
  val onClose: () -> Unit,
  val onConfirm: () -> Unit,
  val onQueryChange: (String) -> Unit,
  val onStatusFilterToggle: (ExpiryStatus) -> Unit,
  val onCategoryFilterToggle: (String) -> Unit,
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
      onStatusFilterToggle = viewModel::onStatusFilterToggle,
      onCategoryFilterToggle = viewModel::onCategoryFilterToggle,
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
  openMenu: PickerMenu? = null,
) {
  AppScaffold(
    modifier = modifier,
    topBar = { PickerTopBar(onClose = actions.onClose) },
    bottomBar = {
      PickerBottomBar(
        count = state.selectedCount,
        expiredCount = state.expiredSelectedCount,
        onConfirm = actions.onConfirm,
      )
    },
  ) { insets ->
    ResponsiveCenteredContainer {
      Column(
        modifier = Modifier.fillMaxSize().padding(insets).padding(horizontal = ScreenGutter),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        SearchField(query = state.query, onQueryChange = actions.onQueryChange)
        FilterChipsRow(state = state, actions = actions, openMenu = openMenu)
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

/** Which dropdown starts open; only previews set it. */
enum class PickerMenu { Status, Category }

@Composable
private fun FilterChipsRow(
  state: IngredientPickerState,
  actions: IngredientPickerActions,
  openMenu: PickerMenu?,
) {
  if (!state.showStatusFilter && !state.showCategoryFilter) return
  Row(
    modifier = Modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (state.showStatusFilter) {
      DropdownFilterChip(
        label = stringResource(R.string.transform_picker_filter_status),
        selectedCount = state.statusFilters.size,
        initiallyExpanded = openMenu == PickerMenu.Status,
      ) {
        state.availableStatuses.forEach { status ->
          FilterMenuItem(
            label = status.label(),
            checked = status in state.statusFilters,
            onToggle = { actions.onStatusFilterToggle(status) },
            leading = { StatusDot(status) },
          )
        }
      }
    }
    if (state.showCategoryFilter) {
      DropdownFilterChip(
        label = stringResource(R.string.transform_picker_filter_category),
        selectedCount = state.categoryFilters.size,
        initiallyExpanded = openMenu == PickerMenu.Category,
      ) {
        state.availableCategories.forEach { category ->
          FilterMenuItem(
            label = category.name,
            checked = category.id in state.categoryFilters,
            onToggle = { actions.onCategoryFilterToggle(category.id) },
          )
        }
      }
    }
  }
}

@Composable
private fun DropdownFilterChip(
  label: String,
  selectedCount: Int,
  initiallyExpanded: Boolean,
  menu: @Composable ColumnScope.() -> Unit,
) {
  var expanded by remember { mutableStateOf(initiallyExpanded) }
  val scheme = CaducityTheme.colorScheme
  val selected = selectedCount > 0
  Box {
    FilterChip(
      selected = selected,
      onClick = { expanded = true },
      shape = CaducityShapes.chip,
      border =
        FilterChipDefaults.filterChipBorder(
          enabled = true,
          selected = selected,
          borderColor = scheme.outlineVariant,
          selectedBorderColor = Color.Transparent,
        ),
      colors =
        FilterChipDefaults.filterChipColors(
          containerColor = Color.Transparent,
          labelColor = scheme.onSurface,
          selectedContainerColor = scheme.primaryContainer,
          selectedLabelColor = scheme.onPrimaryContainer,
          selectedTrailingIconColor = scheme.onPrimaryContainer,
        ),
      label = {
        Text(
          text = if (selected) stringResource(R.string.transform_picker_filter_count, label, selectedCount) else label,
          style = CaducityTheme.typography.labelLarge,
        )
      },
      trailingIcon = {
        Icon(
          imageVector = if (expanded) Symbols.Outlined.ExpandLess else Symbols.Outlined.ExpandMore,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
      },
    )
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, content = menu)
  }
}

@Composable
private fun FilterMenuItem(
  label: String,
  checked: Boolean,
  onToggle: () -> Unit,
  leading: (@Composable () -> Unit)? = null,
) {
  DropdownMenuItem(
    text = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        leading?.invoke()
        Text(label, style = CaducityTheme.typography.bodyLarge)
      }
    },
    onClick = onToggle,
    leadingIcon = { Checkbox(checked = checked, onCheckedChange = null) },
  )
}

@Composable
private fun StatusDot(status: ExpiryStatus) {
  Spacer(Modifier.size(8.dp).clip(CircleShape).background(status.colors().strong))
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
    itemsIndexed(groups, key = { _, group -> group.key }, contentType = { _, _ -> "group" }) { index, group ->
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
  val units = unitCountText(group.nearest.unitType, group.unitCount, group.units.sumAmount())
  val statusColors = status.colors()
  val shapes = ListItemDefaults.segmentedShapes(index, count)
  SegmentedListItem(
    checked = selected,
    onCheckedChange = { onToggle() },
    shapes = shapes,
    // Neutral until it is selected; then the row takes the colour of its status.
    colors =
      ListItemDefaults.segmentedColors(
        containerColor = CaducityTheme.colorScheme.surfaceContainer,
        contentColor = CaducityTheme.colorScheme.onSurface,
        // Soft enough for the status pill, which has the full container colour, to stand out.
        selectedContainerColor =
          statusColors.container.copy(alpha = 0.5f).compositeOver(CaducityTheme.colorScheme.surfaceContainer),
        selectedContentColor = statusColors.onContainer,
      ),
    // A thin outline in the status colour, so it shows in any palette.
    modifier =
      Modifier
        .fillMaxWidth()
        .then(if (selected) Modifier.border(1.dp, statusColors.strong, shapes.selectedShape) else Modifier),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    leadingContent = {
      Checkbox(
        checked = selected,
        onCheckedChange = null,
        colors =
          CheckboxDefaults.colors(
            checkedColor = statusColors.strong,
            checkmarkColor = CaducityTheme.colorScheme.surface,
          ),
      )
    },
    supportingContent = {
      Text(
        text = category?.let { stringResource(R.string.transform_picker_meta, it.name, units) } ?: units,
        style = CaducityTheme.typography.bodySmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    },
    trailingContent = { StatusPill(status = status, label = status.label()) },
  ) {
    Text(
      text = group.name,
      style = CaducityTheme.typography.titleSmall,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
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
  expiredCount: Int,
  onConfirm: () -> Unit,
) {
  ResponsiveCenteredContainer(fillHeight = false) {
    Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = ScreenGutter, vertical = 12.dp)) {
      if (expiredCount > 0) {
        ExpiredNote(count = expiredCount)
        Spacer(Modifier.size(12.dp))
      }
      Button(
        onClick = onConfirm,
        enabled = count > 0,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
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
}

/** Warns that the selection includes expired products, so the result will expire today. */
@Composable
private fun ExpiredNote(count: Int) {
  val color = CaducityTheme.colors.expired.onContainer
  Row(
    modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(Symbols.Outlined.Info, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
    Text(
      text = pluralStringResource(R.plurals.transform_picker_expired_note, count, count),
      style = CaducityTheme.typography.bodyMedium,
      color = color,
      modifier = Modifier.padding(start = 10.dp),
    )
  }
}
