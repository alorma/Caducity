package com.alorma.caducity.ui.screen.category

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.ArrowBack
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.Edit
import com.alorma.caducity.base.ui.icons.symbols.MoreVert
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.CategorySection
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductGroup
import com.alorma.caducity.feature.review.InAppReviewManager
import com.alorma.caducity.feature.review.ShowAppReviewFlag
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
import com.alorma.caducity.ui.components.product.ProductGroupRow
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.components.weekstrip.WeekStrip
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import com.alorma.caducity.ui.utils.findActivity
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CategoryScreen(
  args: CategoryArgs,
  onBack: () -> Unit,
  onOpenAdd: () -> Unit,
  onOpenCalendar: () -> Unit,
  onOpenEditor: () -> Unit,
  onOpenItemActions: (itemId: String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CategoryViewModel = koinViewModel { parametersOf(args) },
) {
  TrackScreen(screen = CategoryScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()
  val activity = LocalContext.current.findActivity()
  val inAppReviewManager: InAppReviewManager = koinInject()
  val showAppReviewFlag: ShowAppReviewFlag = koinInject()
  val scope = androidx.compose.runtime.rememberCoroutineScope()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        CategoryNavigationSideEffect.OpenAdd -> onOpenAdd()
        CategoryNavigationSideEffect.OpenCalendar -> onOpenCalendar()
        CategoryNavigationSideEffect.OpenEditor -> onOpenEditor()
        is CategoryNavigationSideEffect.OpenItemActions -> onOpenItemActions(effect.itemId)
        CategoryNavigationSideEffect.Close -> {
          if (showAppReviewFlag.isEnabled() && activity != null) {
            scope.launch { inAppReviewManager.requestReview(activity) }
          }
          onBack()
        }
      }
    }
  }
  SideEffectHandler(viewModel, dialogState, snackbarState)

  // The category vanished (e.g. deleted): nothing left to show.
  LaunchedEffect(state) { if (state is CategoryState.NotFound) onBack() }

  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    topBar = {
      CategoryTopBar(
        categoryName =
          (state as? CategoryState.Ready)
            ?.data
            ?.category
            ?.name
            .orEmpty(),
        enabled = state is CategoryState.Ready,
        onBack = onBack,
        onCalendar = { viewModel.navigate(CategoryNavigation.Calendar) },
        onEdit = { viewModel.navigate(CategoryNavigation.Edit) },
        onDelete = viewModel::onDeleteClick,
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { viewModel.navigate(CategoryNavigation.AddItem("fab")) },
        shape = CaducityShapes.fab,
        containerColor = CaducityTheme.colorScheme.primary,
        contentColor = CaducityTheme.colorScheme.onPrimary,
        modifier = Modifier.height(64.dp),
        icon = { Icon(Symbols.Outlined.Add, contentDescription = null) },
        text = { Text(stringResource(R.string.category_add)) },
      )
    },
  ) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      when (val current = state) {
        CategoryState.Loading, CategoryState.NotFound -> Unit
        is CategoryState.Ready ->
          CategoryContent(
            state = current,
            onFilterSelected = viewModel::onFilterSelected,
            onDayClick = viewModel::onDayClick,
            onItemClick = { viewModel.navigate(CategoryNavigation.ItemActions(it)) },
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
private fun CategoryTopBar(
  categoryName: String,
  enabled: Boolean,
  onBack: () -> Unit,
  onCalendar: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  var menuOpen by remember { mutableStateOf(false) }
  StyledTopAppBar(
    title = {},
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

/** One entry of the lazy list, so «scroll to this day» is a plain index lookup. */
private sealed interface CategoryItem {
  val key: String

  data object WeekStrip : CategoryItem {
    override val key = "week-strip"
  }

  data object Filters : CategoryItem {
    override val key = "filters"
  }

  data object Empty : CategoryItem {
    override val key = "empty"
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

private fun CategoryState.Ready.items(): List<CategoryItem> =
  buildList {
    add(CategoryItem.WeekStrip)
    add(CategoryItem.Filters)
    if (data.unitCount == 0) add(CategoryItem.Empty)
    visibleSections.forEach { section ->
      add(CategoryItem.SectionHeader(section))
      section.groups.forEachIndexed { index, group -> add(CategoryItem.Product(section, group, index)) }
    }
  }

private fun ProductGroup.expiresOn(day: kotlinx.datetime.LocalDate?) = day != null && units.any { it.expiration == day }

@Composable
private fun CategoryContent(
  state: CategoryState.Ready,
  onFilterSelected: (ExpiryStatus?) -> Unit,
  onDayClick: (kotlinx.datetime.LocalDate) -> Unit,
  onItemClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val data = state.data
  val items = state.items()
  val listState = rememberLazyListState()
  var expanded by rememberSaveable { mutableStateOf(emptySet<String>()) }

  // P19: a tapped day highlights its rows and scrolls to the first one; it never filters.
  LaunchedEffect(state.selectedDay) {
    val index = items.indexOfFirst { it is CategoryItem.Product && it.group.expiresOn(state.selectedDay) }
    if (state.selectedDay != null && index >= 0) listState.animateScrollToItem(index)
  }
  // Opened for a product (notification scheduled before the redesign): show and highlight it.
  LaunchedEffect(state.highlightProductId) {
    val id = state.highlightProductId ?: return@LaunchedEffect
    val index = items.indexOfFirst { it is CategoryItem.Product && it.group.productId == id }
    if (index >= 0) listState.animateScrollToItem(index)
  }

  Column(modifier = modifier.fillMaxSize()) {
    // The header is fixed: it does not scroll with the list.
    CategoryHeader(state)
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
          CategoryItem.Empty ->
            Text(
              text = stringResource(R.string.category_empty),
              style = CaducityTheme.typography.bodyMedium,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 24.dp),
            )
          is CategoryItem.SectionHeader -> SectionHeader(item.section, Modifier.padding(top = 14.dp, bottom = 6.dp))
          is CategoryItem.Product ->
            ProductGroupRow(
              group = item.group,
              today = data.today,
              index = item.index,
              count = item.section.groups.size,
              expanded = item.group.key in expanded || item.group.productId == state.highlightProductId,
              onToggleExpanded = {
                expanded = if (item.group.key in expanded) expanded - item.group.key else expanded + item.group.key
              },
              onUnitClick = onItemClick,
              highlighted =
                item.group.expiresOn(state.selectedDay) ||
                  (state.highlightProductId != null && item.group.productId == state.highlightProductId),
            )
        }
      }
    }
  }
}

@Composable
private fun CategoryHeader(state: CategoryState.Ready) {
  val category = state.data.category
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    CategoryShapeIcon(category.shape, CategoryIcons.vectorOf(category.icon), size = 80.dp)
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = category.name,
        style = CaducityTheme.typography.headlineLarge,
        modifier = Modifier.semantics { heading() },
      )
      Text(
        text = categoryMeta(state.data.unitCount, state.data.nearest, state.data.today),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
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
