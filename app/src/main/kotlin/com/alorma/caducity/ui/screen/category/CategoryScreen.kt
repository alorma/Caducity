package com.alorma.caducity.ui.screen.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.ArrowBack
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.Edit
import com.alorma.caducity.base.ui.icons.symbols.Event
import com.alorma.caducity.base.ui.icons.symbols.MoreVert
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.TouchApp
import com.alorma.caducity.domain.model.CategorySection
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductGroup
import com.alorma.caducity.domain.model.ProductRowDetail
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.domain.model.toProductRow
import com.alorma.caducity.feature.tracking.CategoryScreen as CategoryScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.categoryMeta
import com.alorma.caducity.ui.components.chips.FilterChipOption
import com.alorma.caducity.ui.components.chips.FilterChips
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.product.ProductRow
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.StyledLargeTopAppBar
import com.alorma.caducity.ui.components.weekstrip.WeekStrip
import com.alorma.caducity.ui.navigation.AddFab
import com.alorma.caducity.ui.screen.home.capitalizeFirst
import com.alorma.caducity.ui.screen.home.weekdayDayText
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CategoryScreen(
  args: CategoryArgs,
  onBack: () -> Unit,
  onOpenAdd: () -> Unit,
  onOpenCalendar: () -> Unit,
  onOpenEditor: () -> Unit,
  onOpenItemActions: (itemIds: List<String>) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CategoryViewModel = koinViewModel { parametersOf(args) },
) {
  TrackScreen(screen = CategoryScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        CategoryNavigationSideEffect.OpenAdd -> onOpenAdd()
        CategoryNavigationSideEffect.OpenCalendar -> onOpenCalendar()
        CategoryNavigationSideEffect.OpenEditor -> onOpenEditor()
        is CategoryNavigationSideEffect.OpenItemActions -> onOpenItemActions(effect.itemIds)
        CategoryNavigationSideEffect.Close -> onBack()
      }
    }
  }
  SideEffectHandler(viewModel, dialogState, snackbarState)

  // The category vanished (e.g. deleted): nothing left to show.
  LaunchedEffect(state) { if (state is CategoryState.NotFound) onBack() }

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    topBar = {
      CategoryTopBar(
        ready = state as? CategoryState.Ready,
        scrollBehavior = scrollBehavior,
        onBack = onBack,
        onCalendar = { viewModel.navigate(CategoryNavigation.Calendar) },
        onEdit = { viewModel.navigate(CategoryNavigation.Edit) },
        onDelete = viewModel::onDeleteClick,
      )
    },
    floatingActionButton = {
      ResponsiveCenteredContainer(fillHeight = false) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), contentAlignment = Alignment.CenterEnd) {
          AddFab(
            contentDescription =
              stringResource(
                R.string.category_add_to,
                (state as? CategoryState.Ready)
                  ?.data
                  ?.category
                  ?.name
                  .orEmpty(),
              ),
            onClick = { viewModel.navigate(CategoryNavigation.AddItem("fab")) },
          )
        }
      }
    },
  ) { padding ->
    ResponsiveCenteredContainer(
      modifier = Modifier.padding(padding).nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
      when (val current = state) {
        CategoryState.Loading, CategoryState.NotFound -> Unit
        is CategoryState.Ready ->
          CategoryContent(
            state = current,
            onFilterSelected = viewModel::onFilterSelected,
            onDayClick = viewModel::onDayClick,
            onItemClick = { viewModel.navigate(CategoryNavigation.ItemActions(it)) },
            onConsume = viewModel::onConsume,
            onFreeze = viewModel::onFreeze,
            onDiscard = viewModel::onDiscard,
          )
      }
    }
  }
}

@Composable
private fun SideEffectHandler(
  viewModel: CategoryViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
) {
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is CategorySideEffect.ShowDeleteConfirm ->
          launch {
            val result =
              dialogState.showAlertDialog(
                title = { Text(stringResource(R.string.category_delete_title, effect.name)) },
                content = {
                  Text(pluralStringResource(R.plurals.category_delete_body, effect.unitCount, effect.unitCount))
                },
                positiveButton = { Text(stringResource(R.string.category_delete_confirm)) },
                negativeButton = { Text(stringResource(R.string.category_delete_cancel)) },
                type = AppFeedbackType.Error,
              )
            if (result == DialogResult.Positive) viewModel.onDeleteConfirmed()
          }
        CategorySideEffect.DeleteFailed ->
          launch {
            snackbarState.showSnackbar(message = R.string.error_delete_category_failed, type = AppFeedbackType.Error)
          }
      }
    }
  }
}

@Composable
internal fun CategoryTopBar(
  ready: CategoryState.Ready?,
  scrollBehavior: TopAppBarScrollBehavior,
  onBack: () -> Unit,
  onCalendar: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  var menuOpen by remember { mutableStateOf(false) }
  val enabled = ready != null
  val categoryName = ready?.let { it.data.category.name }.orEmpty()
  StyledLargeTopAppBar(
    scrollBehavior = scrollBehavior,
    title = {
      ready?.let {
        val category = it.data.category
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          CategoryShapeIcon(category.shape, CategoryIcons.vectorOf(category.icon), size = 40.dp)
          Text(text = category.name, maxLines = 1, modifier = Modifier.semantics { heading() })
        }
      }
    },
    subtitle = {
      ready?.let {
        Text(text = categoryMeta(it.data.unitCount, it.data.nearest, it.data.today))
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(Symbols.Outlined.ArrowBack, contentDescription = stringResource(R.string.calendar_back))
      }
    },
    actions = {
      IconButton(onClick = onCalendar, enabled = enabled) {
        Icon(
          Symbols.Outlined.CalendarMonth,
          contentDescription = stringResource(R.string.category_calendar_cd, categoryName),
        )
      }
      Box {
        IconButton(onClick = { menuOpen = true }, enabled = enabled) {
          Icon(Symbols.Outlined.MoreVert, contentDescription = stringResource(R.string.category_menu))
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
          DropdownMenuItem(
            text = { Text(stringResource(R.string.category_menu_edit)) },
            leadingIcon = { Icon(Symbols.Outlined.Edit, contentDescription = null) },
            onClick = {
              menuOpen = false
              onEdit()
            },
          )
          DropdownMenuItem(
            text = { Text(stringResource(R.string.category_menu_delete)) },
            leadingIcon = { Icon(Symbols.Outlined.Delete, contentDescription = null) },
            onClick = {
              menuOpen = false
              onDelete()
            },
          )
        }
      }
    },
  )
}

/** One entry of the lazy list, so «scroll to this product» is a plain index lookup. */
private sealed interface CategoryItem {
  val key: String

  data object WeekStrip : CategoryItem {
    override val key = "week-strip"
  }

  data object Filters : CategoryItem {
    override val key = "filters"
  }

  data object DayFilter : CategoryItem {
    override val key = "day-filter"
  }

  data object DayHint : CategoryItem {
    override val key = "day-hint"
  }

  data object Empty : CategoryItem {
    override val key = "empty"
  }

  data object DayEmpty : CategoryItem {
    override val key = "day-empty"
  }

  data class DayRow(
    val row: ProductRowModel,
    val index: Int,
    val count: Int,
  ) : CategoryItem {
    override val key = "day-${row.key}"
  }

  data class SectionHeader(
    val section: CategorySection,
  ) : CategoryItem {
    override val key = "section-${section.status}"
  }

  data class Product(
    val section: CategorySection,
    val group: ProductGroup,
    val index: Int,
  ) : CategoryItem {
    override val key = "product-${group.key}-${section.status}"
  }
}

/** The units of a selected day grouped by product: one row each, its buttons acting on the nearest unit. */
private fun List<DayUnit>.toDayRows(today: LocalDate): List<ProductRowModel> =
  groupBy { it.unit.groupKey }.values.map { units ->
    units.map { it.unit }.toProductRow(category = null, detail = ProductRowDetail.WithDays, today = today)
  }

private fun CategoryState.Ready.items(): List<CategoryItem> =
  buildList {
    add(CategoryItem.WeekStrip)
    if (selectedDay != null) {
      // Day filter: only the units that expire that day, one row per product, in one connected list (no sections).
      add(CategoryItem.DayFilter)
      val rows = dayUnits.toDayRows(data.today)
      if (rows.isEmpty()) {
        add(CategoryItem.DayEmpty)
      } else {
        rows.forEachIndexed { index, row -> add(CategoryItem.DayRow(row, index, rows.size)) }
      }
      add(CategoryItem.DayHint)
      return@buildList
    }
    add(CategoryItem.Filters)
    if (data.unitCount == 0) add(CategoryItem.Empty)
    visibleSections.forEach { section ->
      add(CategoryItem.SectionHeader(section))
      section.groups.forEachIndexed { index, group -> add(CategoryItem.Product(section, group, index)) }
    }
  }

@Composable
internal fun CategoryContent(
  state: CategoryState.Ready,
  onFilterSelected: (ExpiryStatus?) -> Unit,
  onDayClick: (kotlinx.datetime.LocalDate) -> Unit,
  onItemClick: (List<String>) -> Unit,
  onConsume: (ProductRowModel) -> Unit,
  onFreeze: (ProductRowModel) -> Unit,
  onDiscard: (ProductRowModel) -> Unit,
  modifier: Modifier = Modifier,
) {
  val data = state.data
  val items = state.items()
  val listState = rememberLazyListState()

  // Picking a day filters the list; keep the strip in view while the filter is on.
  LaunchedEffect(state.selectedDay) {
    if (state.selectedDay != null) listState.animateScrollToItem(0)
  }
  // Opened for a product (notification scheduled before the redesign): scroll to it.
  LaunchedEffect(state.highlightProductId) {
    val id = state.highlightProductId ?: return@LaunchedEffect
    val index = items.indexOfFirst { it is CategoryItem.Product && it.group.productId == id }
    if (index >= 0) listState.animateScrollToItem(index)
  }

  Column(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      state = listState,
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 112.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      itemsIndexed(items, key = { _, item -> item.key }) { _, item ->
        when (item) {
          CategoryItem.WeekStrip ->
            WeekStrip(
              data = state.weekStrip,
              selected = state.selectedDay,
              onDayClick = onDayClick,
              modifier = Modifier.padding(bottom = 12.dp),
            )
          CategoryItem.Filters ->
            CategoryFilters(state, onFilterSelected, Modifier.padding(bottom = 8.dp))
          CategoryItem.DayFilter ->
            DayFilter(
              day = state.selectedDay!!,
              unitCount = state.dayUnits.size,
              onClear = { onDayClick(state.selectedDay) },
              modifier = Modifier.padding(bottom = 12.dp),
            )
          CategoryItem.DayHint ->
            Row(
              modifier = Modifier.padding(top = 20.dp, start = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Icon(
                Symbols.Outlined.TouchApp,
                contentDescription = null,
                tint = CaducityTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = stringResource(R.string.category_day_hint),
                style = CaducityTheme.typography.bodyMedium,
                color = CaducityTheme.colorScheme.onSurfaceVariant,
              )
            }
          CategoryItem.DayEmpty ->
            Text(
              text = stringResource(R.string.category_day_empty, data.category.name),
              style = CaducityTheme.typography.bodyMedium,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            )
          is CategoryItem.DayRow ->
            ProductRow(
              model = item.row,
              index = item.index,
              count = item.count,
              onClick = { onItemClick(item.row.itemIds) },
              onConsume = { onConsume(item.row) },
              onFreeze = { onFreeze(item.row) },
              onDiscard = { onDiscard(item.row) },
            )
          CategoryItem.Empty ->
            Text(
              text = stringResource(R.string.category_empty),
              style = CaducityTheme.typography.bodyMedium,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 24.dp),
            )
          is CategoryItem.SectionHeader -> SectionHeader(item.section, Modifier.padding(top = 14.dp, bottom = 6.dp))
          is CategoryItem.Product -> {
            val row =
              item.group.units.toProductRow(
                category = null,
                detail = ProductRowDetail.WithDays,
                today = state.data.today,
              )
            ProductRow(
              model = row,
              index = item.index,
              count = item.section.groups.size,
              onClick = { onItemClick(row.itemIds) },
              onConsume = { onConsume(row) },
              onFreeze = { onFreeze(row) },
              onDiscard = { onDiscard(row) },
            )
          }
        }
      }
    }
  }
}

/** Active day filter: «Dimecres 14 ✕» plus how many units expire that day. */
@Composable
private fun DayFilter(
  day: kotlinx.datetime.LocalDate,
  unitCount: Int,
  onClear: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Row(
      modifier =
        Modifier
          .height(36.dp)
          .clip(CaducityShapes.chip)
          .background(CaducityTheme.colorScheme.primary)
          .clickable(
            onClickLabel = stringResource(R.string.category_day_clear),
            role = Role.Button,
            onClick = onClear,
          ).padding(start = 12.dp, end = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        Symbols.Outlined.Event,
        contentDescription = null,
        tint = CaducityTheme.colorScheme.onPrimary,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = day.weekdayDayText().capitalizeFirst(),
        style = CaducityTheme.typography.labelLarge,
        color = CaducityTheme.colorScheme.onPrimary,
      )
      Icon(
        Symbols.Outlined.Close,
        contentDescription = stringResource(R.string.category_day_clear),
        tint = CaducityTheme.colorScheme.onPrimary,
        modifier = Modifier.padding(start = 2.dp).size(18.dp),
      )
    }
    Text(
      text = pluralStringResource(R.plurals.category_day_units, unitCount, unitCount),
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun statusLabel(status: ExpiryStatus): String =
  stringResource(
    when (status) {
      ExpiryStatus.EXPIRED -> R.string.category_status_expired
      ExpiryStatus.SOON -> R.string.category_status_soon
      ExpiryStatus.FRESH -> R.string.category_status_fresh
      ExpiryStatus.FROZEN -> R.string.category_status_frozen
    },
  )

@Composable
private fun CategoryFilters(
  state: CategoryState.Ready,
  onFilterSelected: (ExpiryStatus?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val statuses =
    state.data.statusCounts.keys
      .toList()
      .let { present -> if (state.filter != null && state.filter !in present) present + state.filter else present }
  val options =
    listOf(FilterChipOption(stringResource(R.string.category_filter_all), state.data.unitCount)) +
      statuses.map { FilterChipOption(statusLabel(it), state.data.statusCounts[it] ?: 0, it) }
  FilterChips(
    options = options,
    selectedIndex = if (state.filter == null) 0 else statuses.indexOf(state.filter) + 1,
    onSelect = { index -> onFilterSelected(if (index == 0) null else statuses[index - 1]) },
    modifier = modifier,
  )
}

@Composable
private fun SectionHeader(
  section: CategorySection,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Spacer(Modifier.size(10.dp).clip(CircleShape).background(section.status.colors().strong))
    Text(
      text = statusLabel(section.status),
      style = CaducityTheme.typography.titleLarge,
      modifier = Modifier.semantics { heading() },
    )
    Text(
      text = section.unitCount.toString(),
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
  }
}
