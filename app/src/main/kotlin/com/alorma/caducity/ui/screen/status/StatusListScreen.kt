package com.alorma.caducity.ui.screen.status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.ArrowBack
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Checklist
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.EventRepeat
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.WaterDrop
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.feature.tracking.StatusListScreen as StatusListScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.chips.QuickDateChips
import com.alorma.caducity.ui.components.chips.QuickDateOption
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.bottomsheet.rememberAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.product.shortDateText
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
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
  modifier: Modifier = Modifier,
  viewModel: StatusListViewModel = koinViewModel { parametersOf(status) },
) {
  TrackScreen(screen = StatusListScreenEvent(status.name.lowercase()))
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = rememberAppDialogState()
  val bottomSheetState = rememberAppBottomSheetState(skipHalfExpanded = true)
  val scope = rememberCoroutineScope()
  val colors = status.colors()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        is StatusListNavigationSideEffect.OpenItemActions -> onOpenItemActions(effect.itemId)
        StatusListNavigationSideEffect.OpenAdd -> onOpenAdd()
      }
    }
  }
  SideEffectHandler(viewModel, dialogState, bottomSheetState, scope)

  val ready = state as? StatusListState.Ready
  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    bottomSheetState = bottomSheetState,
    topBar = {
      StyledTopAppBar(
        title = {},
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = colors.container,
            navigationIconContentColor = colors.onContainer,
          ),
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Symbols.Outlined.ArrowBack, contentDescription = stringResource(R.string.calendar_back))
          }
        },
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
        )
      }
    },
  ) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      if (ready != null) {
        StatusListContent(
          ready = ready,
          viewModel = viewModel,
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
                datePickerState.setSelection(effect.target.proposed ?: todayProvider.today().plus(1, DateTimeUnit.DAY))
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

/** Quick dates for «Canvia la data» and «Descongela»: a few offsets from today plus the calendar. */
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
        text =
          stringResource(
            if (target.unfreeze) R.string.status_dates_unfreeze_title else R.string.item_actions_change_date,
          ),
        style = CaducityTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() },
      )
      val options =
        buildList {
          target.proposed?.let { add(stringResource(R.string.status_dates_as_before, it.shortDateText()) to it) }
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
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    // The header is fixed: it does not scroll with the list.
    StatusHeader(
      ready = ready,
      onToggleSelecting = viewModel::onToggleSelecting,
      onDiscardAll = viewModel::onDiscardAllClick,
    )
    if (ready.isEmpty) {
      StatusEmpty(ready.status, onAdd = { viewModel.navigate(StatusListNavigation.AddFirst) })
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
      ) {
        ready.sections.forEach { section ->
          item(key = "header-${section.category.id}") {
            SectionHeader(section, Modifier.padding(top = 16.dp, bottom = 8.dp))
          }
          items(section.rows, key = { it.key }) { row ->
            val index = section.rows.indexOf(row)
            StatusRowItem(
              row = row,
              index = index,
              count = section.rows.size,
              ready = ready,
              onClick = {
                if (ready.selecting) {
                  viewModel.onToggleRow(row.key)
                } else {
                  viewModel.navigate(StatusListNavigation.ItemActions(row.nearest.itemId))
                }
              },
              viewModel = viewModel,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatusHeader(
  ready: StatusListState.Ready,
  onToggleSelecting: () -> Unit,
  onDiscardAll: () -> Unit,
) {
  val colors = ready.status.colors()
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
        .background(colors.container)
        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = pluralStringResource(R.plurals.home_product_count, ready.totalCount, ready.totalCount).uppercase(),
      style = CaducityTheme.typography.labelMedium,
      color = colors.onContainer,
    )
    Text(
      text = stringResource(titleRes(ready.status)),
      style = CaducityTheme.typography.displaySmall,
      color = colors.onContainer,
      modifier = Modifier.semantics { heading() },
    )
    Text(
      text = helpText(ready),
      style = CaducityTheme.typography.bodyMedium,
      color = colors.onContainer,
    )
    if (!ready.isEmpty) {
      Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedButton(
          onClick = onToggleSelecting,
          border = BorderStroke(1.dp, colors.onContainer),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onContainer),
          modifier = Modifier.heightIn(min = 44.dp),
        ) {
          if (!ready.selecting) {
            Icon(Symbols.Outlined.Checklist, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
          }
          Text(stringResource(if (ready.selecting) R.string.status_select_done else R.string.status_select))
        }
        if (ready.status == ExpiryStatus.EXPIRED && !ready.selecting) {
          Button(
            onClick = onDiscardAll,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = colors.strong,
                contentColor = CaducityTheme.colorScheme.surface,
              ),
            modifier = Modifier.heightIn(min = 44.dp),
          ) {
            Icon(Symbols.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.status_discard_all))
          }
        }
      }
    }
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
    ExpiryStatus.SOON -> pluralStringResource(R.plurals.status_help_soon, ready.soonDays, ready.soonDays)
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
    Text(
      text = section.category.name,
      style = CaducityTheme.typography.titleMedium,
      modifier = Modifier.weight(1f).semantics { heading() },
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

@Composable
private fun StatusRowItem(
  row: StatusRow,
  index: Int,
  count: Int,
  ready: StatusListState.Ready,
  onClick: () -> Unit,
  viewModel: StatusListViewModel,
) {
  val colors = ready.status.colors()
  val quantity =
    row.legacyPack?.let { stringResource(R.string.item_pack_of, it) }
      ?: row.unitCount.takeIf { it > 1 }?.let { pluralStringResource(R.plurals.home_unit_count, it, it) }
  ConnectedRow(
    index = index,
    count = count,
    onClick = onClick,
    modifier = Modifier.heightIn(min = 64.dp),
    contentPadding = PaddingValues(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
  ) {
    if (ready.selecting) {
      Checkbox(
        checked = row.key in ready.selectedKeys,
        onCheckedChange = null,
        modifier = Modifier.padding(end = 12.dp),
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = if (quantity != null) "${row.name} · $quantity" else row.name,
        style = CaducityTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = rowDateText(ready.status, row, ready.today),
        style = CaducityTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        color = colors.onContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    if (!ready.selecting) {
      Spacer(Modifier.width(8.dp))
      RowButtons(ready.status, row, viewModel)
    }
  }
}

@Composable
private fun RowButtons(
  status: ExpiryStatus,
  row: StatusRow,
  viewModel: StatusListViewModel,
) {
  val frozenColor = CaducityTheme.colors.frozen.strong
  val expiredColor = CaducityTheme.colors.expired.strong
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    when (status) {
      ExpiryStatus.EXPIRED -> {
        RoundButton(
          Symbols.Outlined.EventRepeat,
          stringResource(R.string.status_action_reschedule, row.name),
          onClick = { viewModel.onRescheduleClick(row) },
        )
        RoundButton(
          Symbols.Outlined.Delete,
          stringResource(R.string.status_action_discard, row.name),
          tint = expiredColor,
          onClick = { viewModel.onDiscard(row) },
        )
      }
      ExpiryStatus.SOON, ExpiryStatus.FRESH -> {
        RoundButton(
          Symbols.Outlined.Check,
          stringResource(R.string.home_mark_consumed, row.name),
          onClick = { viewModel.onConsume(row) },
        )
        RoundButton(
          Symbols.Outlined.AcUnit,
          stringResource(R.string.status_action_freeze, row.name),
          tint = if (status == ExpiryStatus.SOON) frozenColor else null,
          onClick = { viewModel.onFreeze(row) },
        )
      }
      ExpiryStatus.FROZEN -> {
        RoundButton(
          Symbols.Outlined.Check,
          stringResource(R.string.home_mark_consumed, row.name),
          onClick = { viewModel.onConsume(row) },
        )
        RoundButton(
          Symbols.Outlined.WaterDrop,
          stringResource(R.string.status_action_unfreeze, row.name),
          tint = frozenColor,
          onClick = { viewModel.onUnfreezeClick(row) },
        )
      }
    }
  }
}

@Composable
private fun RoundButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  tint: androidx.compose.ui.graphics.Color? = null,
) {
  IconButton(
    onClick = onClick,
    modifier = Modifier.size(44.dp).clip(CircleShape).background(CaducityTheme.colorScheme.surface),
  ) {
    Icon(icon, contentDescription = contentDescription, tint = tint ?: CaducityTheme.colorScheme.onSurface)
  }
}

private fun LocalDate.dayMonthText(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("d MMM", locale).format(toJavaLocalDate())

/** «Fa 1 dia · ds. 10 oct», «Avui · dg. 11 oct», «D'aquí 22 dies · dl. 2 nov» or «Congelat el 2 oct». */
@Composable
private fun rowDateText(
  status: ExpiryStatus,
  row: StatusRow,
  today: LocalDate,
): String {
  if (status == ExpiryStatus.FROZEN) {
    return stringResource(R.string.status_date_frozen, (row.frozenSince ?: row.expiration).dayMonthText())
  }
  val days = today.daysUntil(row.expiration)
  val date = row.expiration.shortDateText()
  return when {
    days == 0 -> stringResource(R.string.status_date_today, date)
    days == 1 -> stringResource(R.string.status_date_tomorrow, date)
    days > 1 -> stringResource(R.string.status_date_in_days, days, date)
    days == -1 -> stringResource(R.string.status_date_ago_one, date)
    else -> stringResource(R.string.status_date_ago_days, -days, date)
  }
}

@Composable
private fun SelectionBar(
  ready: StatusListState.Ready,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDiscard: () -> Unit,
  onReschedule: () -> Unit,
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
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (ready.status) {
          ExpiryStatus.EXPIRED -> {
            BarButton(
              stringResource(R.string.status_bulk_discard, units),
              enabled,
              onDiscard,
              Modifier.weight(1f),
              danger = true,
            )
            BarButton(
              stringResource(R.string.status_bulk_reschedule, units),
              enabled,
              onReschedule,
              Modifier.weight(1f),
            )
          }
          ExpiryStatus.SOON, ExpiryStatus.FRESH -> {
            BarButton(stringResource(R.string.status_bulk_consume, units), enabled, onConsume, Modifier.weight(1f))
            BarButton(stringResource(R.string.status_bulk_freeze, units), enabled, onFreeze, Modifier.weight(1f))
          }
          ExpiryStatus.FROZEN ->
            BarButton(stringResource(R.string.status_bulk_consume, units), enabled, onConsume, Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
private fun BarButton(
  text: String,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  danger: Boolean = false,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    shape = CaducityShapes.chip,
    colors =
      if (danger) {
        ButtonDefaults.buttonColors(
          containerColor = CaducityTheme.colors.expired.strong,
          contentColor = CaducityTheme.colorScheme.surface,
        )
      } else {
        ButtonDefaults.buttonColors()
      },
    modifier = modifier.height(48.dp),
  ) {
    Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
