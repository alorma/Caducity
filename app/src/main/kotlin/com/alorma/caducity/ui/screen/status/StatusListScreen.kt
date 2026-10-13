package com.alorma.caducity.ui.screen.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Checklist
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.DeleteSweep
import com.alorma.caducity.base.ui.icons.symbols.EventRepeat
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductRowDetail
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.domain.model.toProductRow
import com.alorma.caducity.feature.tracking.StatusListScreen as StatusListScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.chips.QuickDateChips
import com.alorma.caducity.ui.components.chips.QuickDateOption
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.bottomsheet.rememberAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.product.ProductRow
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun StatusListScreen(
  status: ExpiryStatus,
  onBack: () -> Unit,
  onOpenItemActions: (itemId: String) -> Unit,
  onOpenAdd: () -> Unit,
  onOpenTransform: (itemIds: List<String>) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: StatusListViewModel = koinViewModel { parametersOf(status) },
) {
  TrackScreen(screen = StatusListScreenEvent(status.name.lowercase()))
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = rememberAppDialogState()
  val bottomSheetState = rememberAppBottomSheetState(skipHalfExpanded = true)
  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()
  val headerState = rememberStatusHeaderState()
  val connection = remember(listState) { headerState.connection(listState) }

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        is StatusListNavigationSideEffect.OpenItemActions -> onOpenItemActions(effect.itemId)
        StatusListNavigationSideEffect.OpenAdd -> onOpenAdd()
        is StatusListNavigationSideEffect.OpenTransform -> onOpenTransform(effect.itemIds)
      }
    }
  }
  SideEffectHandler(viewModel, dialogState, bottomSheetState, scope)

  val ready = state as? StatusListState.Ready
  headerState.enabled = ready?.selecting != true
  AppScaffold(
    modifier = modifier.nestedScroll(connection),
    dialogState = dialogState,
    bottomSheetState = bottomSheetState,
    topBar = {
      StatusHeader(
        status = status,
        count = ready?.totalCount ?: 0,
        title = stringResource(titleRes(status)),
        help = ready?.let { helpText(it) }.orEmpty(),
        actions = ready?.takeIf { !it.isEmpty }?.let { headerActions(it, viewModel) }.orEmpty(),
        state = headerState,
        onBack = onBack,
        selectionCount = ready?.takeIf { it.selecting }?.selectedUnitCount,
        onCloseSelection = viewModel::onToggleSelecting,
      )
    },
    bottomBar = {
      if (ready != null && ready.selecting) {
        SelectionBar(
          ready = ready,
          onConsume = viewModel::onBulkConsume,
          onFreeze = viewModel::onBulkFreeze,
          onDiscard = viewModel::onBulkDiscardClick,
          onReschedule = viewModel::onBulkRescheduleClick,
          onTransform = { viewModel.navigate(StatusListNavigation.TransformSelection) },
        )
      }
    },
  ) { padding ->
    // The list goes under the header (only the bottom inset is applied) so it shows through its shadow.
    ResponsiveCenteredContainer(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
      if (ready != null) {
        StatusListContent(
          ready = ready,
          viewModel = viewModel,
          listState = listState,
          topPadding = padding.calculateTopPadding(),
        )
      }
    }
  }
}

@Composable
private fun SideEffectHandler(
  viewModel: StatusListViewModel,
  dialogState: AppDialogState,
  bottomSheetState: AppBottomSheetState,
  scope: CoroutineScope,
) {
  val todayProvider: TodayProvider = koinInject()
  val datePickerState = rememberDatePickerState()
  val okLabel = stringResource(R.string.add_picker_ok)
  val cancelLabel = stringResource(R.string.add_picker_cancel)
  val cancelAction = stringResource(R.string.item_actions_cancel)
  val discardLabel = stringResource(R.string.status_discard_confirm)
  val discardBody = stringResource(R.string.status_discard_body)
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is StatusListSideEffect.ShowDiscardConfirm ->
          launch {
            val result =
              dialogState.showAlertDialog(
                title = {
                  Text(pluralStringResource(R.plurals.status_discard_title, effect.unitCount, effect.unitCount))
                },
                content = { Text(discardBody) },
                positiveButton = { Text(discardLabel) },
                negativeButton = { Text(cancelAction) },
                type = AppFeedbackType.Error,
              )
            if (result == DialogResult.Positive) viewModel.onDiscardConfirmed(effect.itemIds)
          }

        is StatusListSideEffect.ShowQuickDates ->
          bottomSheetState.QuickDatesSheet(
            coroutineScope = scope,
            target = effect.target,
            today = todayProvider.today(),
            onDate = { date -> viewModel.onDateChosen(effect.target, date) },
            onPickDate = {
              scope.launch {
                datePickerState.setSelection(todayProvider.today().plus(1, DateTimeUnit.DAY))
                val result =
                  dialogState.showDatePickerDialog(
                    datePickerState = datePickerState,
                    positiveButton = { Text(okLabel) },
                    negativeButton = { Text(cancelLabel) },
                    type = AppFeedbackType.Info,
                  )
                val picked = datePickerState.selectedDateMillis
                if (result == DialogResult.Positive && picked != null) {
                  viewModel.onDateChosen(
                    effect.target,
                    Instant.fromEpochMilliseconds(picked).toLocalDateTime(TimeZone.UTC).date,
                  )
                }
              }
            },
          )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun DatePickerState.setSelection(date: LocalDate) {
  selectedDateMillis = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

/** Quick dates for «Canvia la data» of the selected rows: a few offsets from today plus the calendar. */
private fun AppBottomSheetState.QuickDatesSheet(
  coroutineScope: CoroutineScope,
  target: DateTarget,
  today: LocalDate,
  onDate: (LocalDate) -> Unit,
  onPickDate: () -> Unit,
) {
  show {
    Column(
      modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = stringResource(R.string.item_actions_change_date),
        style = CaducityTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() },
      )
      val options =
        buildList {
          add(stringResource(R.string.status_dates_tomorrow) to today.plus(1, DateTimeUnit.DAY))
          add(stringResource(R.string.status_dates_in_3d) to today.plus(3, DateTimeUnit.DAY))
          add(stringResource(R.string.status_dates_in_1w) to today.plus(7, DateTimeUnit.DAY))
        }
      QuickDateChips(
        options = options.map { QuickDateOption(it.first) },
        onClick = { index ->
          coroutineScope.launch { hide() }
          onDate(options[index].second)
        },
      )
      OutlinedButton(
        onClick = {
          coroutineScope.launch { hide() }
          onPickDate()
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
      ) {
        Icon(Symbols.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.status_dates_pick))
      }
    }
  }
}

@Composable
private fun StatusListContent(
  ready: StatusListState.Ready,
  viewModel: StatusListViewModel,
  listState: LazyListState,
  topPadding: Dp,
  modifier: Modifier = Modifier,
) {
  val today = koinInject<TodayProvider>().today()
  Column(modifier = modifier.fillMaxSize()) {
    if (ready.isEmpty) {
      Box(modifier = Modifier.fillMaxSize().padding(top = topPadding)) {
        StatusEmpty(ready.status, onAdd = { viewModel.navigate(StatusListNavigation.AddFirst) })
      }
    } else {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = topPadding + 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
      ) {
        ready.sections.forEach { section ->
          item(key = "header-${section.category.id}") {
            SectionHeader(section, Modifier.padding(top = 16.dp, bottom = 8.dp))
          }
          items(section.rows, key = { it.key }) { row ->
            val index = section.rows.indexOf(row)
            ProductRow(
              model = row.toRowModel(today),
              index = index,
              count = section.rows.size,
              onClick = {
                if (ready.selecting) {
                  viewModel.onToggleRow(row.key)
                } else {
                  viewModel.navigate(StatusListNavigation.ItemActions(row.nearest.itemId))
                }
              },
              onConsume = { viewModel.onConsume(row) },
              onFreeze = { viewModel.onFreeze(row) },
              onDiscard = { viewModel.onDiscard(row) },
              selected = if (ready.selecting) row.key in ready.selectedKeys else null,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun headerActions(
  ready: StatusListState.Ready,
  viewModel: StatusListViewModel,
): List<HeaderAction> {
  val select =
    HeaderAction(
      label = stringResource(R.string.status_select),
      icon = Symbols.Outlined.Checklist,
      onClick = viewModel::onToggleSelecting,
    )
  return if (ready.status == ExpiryStatus.EXPIRED) {
    listOf(
      HeaderAction(
        label = stringResource(R.string.status_discard_all),
        icon = Symbols.Outlined.DeleteSweep,
        primary = true,
        onClick = viewModel::onDiscardAllClick,
      ),
      select,
    )
  } else {
    listOf(select)
  }
}

private fun titleRes(status: ExpiryStatus): Int =
  when (status) {
    ExpiryStatus.EXPIRED -> R.string.status_title_expired
    ExpiryStatus.SOON -> R.string.status_title_soon
    ExpiryStatus.FRESH -> R.string.status_title_fresh
    ExpiryStatus.FROZEN -> R.string.status_title_frozen
  }

@Composable
private fun helpText(ready: StatusListState.Ready): String =
  when (ready.status) {
    ExpiryStatus.EXPIRED -> stringResource(R.string.status_help_expired)
    ExpiryStatus.SOON -> stringResource(R.string.status_help_soon_margin)
    ExpiryStatus.FRESH -> stringResource(R.string.status_help_fresh)
    ExpiryStatus.FROZEN -> stringResource(R.string.status_help_frozen)
  }

@Composable
private fun SectionHeader(
  section: StatusSection,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    CategoryShapeIcon(section.category.shape, CategoryIcons.vectorOf(section.category.icon), size = 36.dp)
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = section.category.name,
        style = CaducityTheme.typography.titleMedium,
        modifier = Modifier.weight(1f, fill = false).semantics { heading() },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = section.unitCount.toString(),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/** The row as the shared [ProductRow] shows it: no category icon (the list is already grouped by it). */
private fun StatusRow.toRowModel(today: LocalDate): ProductRowModel =
  entries.toProductRow(category = null, detail = ProductRowDetail.WithDays, today = today)

@Composable
private fun SelectionBar(
  ready: StatusListState.Ready,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDiscard: () -> Unit,
  onReschedule: () -> Unit,
  onTransform: () -> Unit,
) {
  val units = ready.selectedUnitCount
  val enabled = units > 0
  Surface(
    color = CaducityTheme.colorScheme.surfaceContainerHigh,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(
          horizontal = 20.dp,
          vertical = 12.dp,
        ),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = pluralStringResource(R.plurals.status_selected_count, units, units),
        style = CaducityTheme.typography.labelLarge,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
      TransformSelectionActions(ready, onConsume, onFreeze, onDiscard, onReschedule, onTransform)
    }
  }
}

@Composable
private fun StatusEmpty(
  status: ExpiryStatus,
  onAdd: () -> Unit,
) {
  val (title, body) =
    when (status) {
      ExpiryStatus.EXPIRED -> R.string.status_empty_expired_title to R.string.status_empty_expired_body
      ExpiryStatus.SOON -> R.string.status_empty_soon_title to R.string.status_empty_soon_body
      ExpiryStatus.FRESH -> R.string.status_empty_fresh_title to R.string.status_empty_fresh_body
      ExpiryStatus.FROZEN -> R.string.status_empty_frozen_title to null
    }
  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(title),
      style = CaducityTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    if (body != null) {
      Text(
        text = stringResource(body),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
      )
    }
    if (status == ExpiryStatus.FRESH) {
      Button(onClick = onAdd, modifier = Modifier.padding(top = 20.dp).heightIn(min = 48.dp)) {
        Text(stringResource(R.string.status_empty_fresh_action))
      }
    }
  }
}

/** Connected group shown only with Transforma on: Consumit · Congela · Transforma (and the variants of Caducats and Congelats). */
@Composable
private fun TransformSelectionActions(
  ready: StatusListState.Ready,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDiscard: () -> Unit,
  onReschedule: () -> Unit,
  onTransform: () -> Unit,
) {
  val units = ready.selectedUnitCount
  val colors = CaducityTheme.colors
  val neutral = CaducityTheme.colorScheme.surfaceContainerHighest
  val onNeutral = CaducityTheme.colorScheme.onSurface
  val consume =
    SelectionAction(
      Symbols.Outlined.Check,
      stringResource(R.string.transform_bar_consume),
      stringResource(R.string.status_bulk_consume, units),
      neutral,
      onNeutral,
      onConsume,
    )
  val freeze =
    SelectionAction(
      Symbols.Outlined.AcUnit,
      stringResource(R.string.transform_bar_freeze),
      stringResource(R.string.status_bulk_freeze, units),
      colors.frozen.container,
      colors.frozen.onContainer,
      onFreeze,
    )
  val discard =
    SelectionAction(
      Symbols.Outlined.Delete,
      stringResource(R.string.transform_bar_discard),
      stringResource(R.string.status_bulk_discard, units),
      colors.expired.container,
      colors.expired.onContainer,
      onDiscard,
    )
  val reschedule =
    SelectionAction(
      Symbols.Outlined.EventRepeat,
      stringResource(R.string.transform_bar_reschedule),
      stringResource(R.string.status_bulk_reschedule, units),
      neutral,
      onNeutral,
      onReschedule,
    )
  val transform =
    SelectionAction(
      AppIcons.Cooking,
      stringResource(R.string.transform_bar_transform),
      stringResource(R.string.transform_bar_transform_description, units),
      CaducityTheme.colorScheme.primary,
      CaducityTheme.colorScheme.onPrimary,
      onTransform,
    )
  val actions =
    when (ready.status) {
      ExpiryStatus.EXPIRED -> listOf(discard, reschedule, transform)
      ExpiryStatus.SOON, ExpiryStatus.FRESH -> listOf(consume, freeze, transform)
      ExpiryStatus.FROZEN -> listOf(consume, transform)
    }
  SelectionActionGroup(actions = actions, enabled = units > 0)
}
