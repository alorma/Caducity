package com.alorma.caducity.ui.components.actions

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.ArrowBack
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.DriveFileMove
import com.alorma.caducity.base.ui.icons.symbols.Edit
import com.alorma.caducity.base.ui.icons.symbols.Error
import com.alorma.caducity.base.ui.icons.symbols.Event
import com.alorma.caducity.base.ui.icons.symbols.ExpandLess
import com.alorma.caducity.base.ui.icons.symbols.ExpandMore
import com.alorma.caducity.base.ui.icons.symbols.Info
import com.alorma.caducity.base.ui.icons.symbols.MoreHoriz
import com.alorma.caducity.base.ui.icons.symbols.Remove
import com.alorma.caducity.base.ui.icons.symbols.Sunny
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.TouchApp
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.chips.QuickDateChips
import com.alorma.caducity.ui.components.chips.QuickDateOption
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.connectedRowColors
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogHost
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.product.shortDateText
import com.alorma.caducity.ui.components.shape.connectedShape
import com.alorma.caducity.ui.components.status.StatusPill
import com.alorma.caducity.ui.components.status.statusPillText
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Content of the actions bottom sheet (`ItemActions` route): everything you can do to a product. One unit,
 * several units, and the expired variants of both (spec §4).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemActionsSheet(
  itemIds: List<String>,
  onClose: () -> Unit,
  onEdit: (itemId: String) -> Unit,
  onTransform: (itemIds: List<String>) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ItemActionsSheetViewModel = koinViewModel { parametersOf(itemIds) },
) {
  val state by viewModel.state.collectAsState()
  val dialogState = rememberAppDialogState()
  val datePickerState = rememberDatePickerState()
  val haptics = LocalHapticFeedback.current

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        ItemActionsSheetNavigationSideEffect.Close -> onClose()
        is ItemActionsSheetNavigationSideEffect.Edit -> onEdit(effect.itemId)
        is ItemActionsSheetNavigationSideEffect.Transform -> onTransform(effect.itemIds)
      }
    }
  }
  SideEffectHandler(
    viewModel = viewModel,
    dialogState = dialogState,
    datePickerState = datePickerState,
    onHaptic = { haptics.performHapticFeedback(it) },
  )

  Box(modifier = modifier) {
    when (val current = state) {
      ItemActionsSheetState.Loading -> Spacer(Modifier.height(160.dp).fillMaxWidth())
      is ItemActionsSheetState.Ready ->
        if (current.pickingCategory) {
          CategoryPicker(
            categories = current.otherCategories,
            onPick = viewModel::onMove,
            onBack = viewModel::onMoveBack,
          )
        } else {
          ActionsContent(state = current, actions = viewModel)
        }
    }
    AppDialogHost(dialogState)
  }
}

@Composable
internal fun ActionsContent(
  state: ItemActionsSheetState.Ready,
  actions: ItemActionsHandlers,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .navigationBarsPadding()
        .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Header(state)
    when {
      state.expired -> ExpiredContent(state, actions)
      state.multiple -> MultipleContent(state, actions)
      else -> SingleContent(state, actions)
    }
  }
}

/** `Accions`: one unit, everything unfolded. */
@Composable
private fun SingleContent(
  state: ItemActionsSheetState.Ready,
  actions: ItemActionsHandlers,
) {
  PrimaryActions(
    consumeLabel = stringResource(R.string.item_actions_consumed),
    freezeLabel =
      stringResource(if (state.frozen) R.string.action_unfreeze else R.string.item_actions_freeze),
    freezeIcon = if (state.frozen) Symbols.Outlined.Sunny else Symbols.Outlined.AcUnit,
    showFreeze = state.frozen || state.freezableCount > 0,
    onConsume = actions::onConsume,
    onFreeze = actions::onFreezeToggle,
  )
  // A frozen unit has no expiration date to change and cannot be turned into something else yet.
  if (state.frozen) {
    UnfreezeHint()
  } else {
    ChangeDate(onQuick = actions::onQuickReschedule, onPick = actions::onPickDate)
  }
  SecondaryActions(actions, showTransform = !state.frozen)
}

/** What «Descongela» does to a single frozen unit; the date picker proposes the same day. */
@Composable
private fun UnfreezeHint(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = CaducityTheme.colorScheme.surfaceContainer,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(
        Symbols.Outlined.Info,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = CaducityTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = stringResource(R.string.unfreeze_hint),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/** `QuantitatAccions`: several units, a selector and «Més accions» folded. */
@Composable
private fun MultipleContent(
  state: ItemActionsSheetState.Ready,
  actions: ItemActionsHandlers,
) {
  QuantitySelector(state = state, onSelect = actions::onSelect)
  if (state.frozenOnly) {
    PrimaryActions(
      consumeLabel = stringResource(R.string.item_actions_consume_n, state.selected),
      freezeLabel = stringResource(R.string.action_unfreeze_n, state.selected),
      freezeIcon = Symbols.Outlined.Sunny,
      showFreeze = true,
      onConsume = actions::onConsume,
      onFreeze = actions::onFreezeToggle,
    )
    MoreActions(open = state.moreOpen, onToggle = actions::onToggleMore) {
      SecondaryActions(actions, showTransform = false)
    }
    return
  }
  PrimaryActions(
    consumeLabel = stringResource(R.string.item_actions_consume_n, state.selected),
    freezeLabel = stringResource(R.string.item_actions_freeze_n, state.freezeCount),
    freezeIcon = Symbols.Outlined.AcUnit,
    showFreeze = state.freezableCount > 0,
    onConsume = actions::onConsume,
    onFreeze = actions::onFreezeToggle,
  )
  MoreActions(open = state.moreOpen, onToggle = actions::onToggleMore) {
    ChangeDate(onQuick = actions::onQuickReschedule, onPick = actions::onPickDate)
    SecondaryActions(actions)
  }
}

/** `AccionsCaducat` and `QuantitatCaducat`: the only big button is «Llença»; eating it anyway is hidden away. */
@Composable
private fun ExpiredContent(
  state: ItemActionsSheetState.Ready,
  actions: ItemActionsHandlers,
) {
  val expired = CaducityTheme.colors.expired
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(expired.container)
        .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(Symbols.Outlined.Error, contentDescription = null, tint = expired.onContainer)
    Spacer(Modifier.width(12.dp))
    Text(
      text =
        stringResource(
          if (state.multiple) R.string.item_actions_expired_warning_many else R.string.item_actions_expired_warning_one,
        ),
      style = CaducityTheme.typography.bodyMedium,
      color = expired.onContainer,
    )
  }
  if (state.multiple) QuantitySelector(state = state, onSelect = actions::onSelect)
  ActionButton(
    label =
      if (state.multiple) {
        stringResource(R.string.item_actions_discard_n, state.selected)
      } else {
        stringResource(R.string.item_actions_discard)
      },
    icon = Symbols.Outlined.Delete,
    container = expired.strong,
    content = CaducityTheme.contentOn(expired.strong),
    shape = RoundedCornerShape(32.dp),
    onClick = actions::onDiscardSelected,
    modifier = Modifier.fillMaxWidth(),
  )
  MoreActions(open = state.moreOpen, onToggle = actions::onToggleMore) {
    val count = 3
    Column(verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing)) {
      HoldToConfirmRow(
        icon = Symbols.Outlined.TouchApp,
        title = stringResource(R.string.item_actions_eat_anyway),
        subtitle = stringResource(R.string.item_actions_eat_anyway_hint),
        accessibilityLabel = stringResource(R.string.item_actions_eat_anyway),
        onConfirmed = actions::onEatAnyway,
        onRequestConfirm = actions::onEatAnywayRequested,
        shape = connectedShape(0, count),
      )
      ActionRow(
        1,
        count,
        Symbols.Outlined.Event,
        stringResource(R.string.item_actions_wrong_date),
        onClick = actions::onFixDate,
      )
      ActionRow(
        2,
        count,
        Symbols.Outlined.Edit,
        stringResource(R.string.item_actions_edit),
        onClick = actions::onEdit,
      )
    }
  }
}

@Composable
private fun Header(
  state: ItemActionsSheetState.Ready,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    CategoryShapeIcon(
      shape = state.category.shape,
      icon = CategoryIcons.vectorOf(state.category.icon),
      size = 56.dp,
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = state.name,
        style = CaducityTheme.typography.titleLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.semantics { heading() },
      )
      Text(
        text = metaText(state),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
    if (!state.multiple) {
      StatusPill(status = state.status, label = statusPillText(state.status, state.daysLeft))
    }
  }
}

@Composable
private fun metaText(state: ItemActionsSheetState.Ready): String {
  val frozenSince = state.frozenSince.takeIf { state.frozenOnly }
  if (frozenSince != null) {
    val day = shortDateText(frozenSince)
    return if (state.multiple) {
      stringResource(R.string.item_actions_frozen_many, state.unitCount, day)
    } else {
      stringResource(R.string.item_actions_frozen_one, day)
    }
  }
  val date = formatDate(state.expiration)
  return when {
    state.multiple && state.expired ->
      stringResource(R.string.item_actions_units_first_expired, state.unitCount, date)
    state.multiple -> stringResource(R.string.item_actions_units_first, state.unitCount, date)
    state.expired -> stringResource(R.string.item_actions_unit_expired, date)
    state.frozen -> stringResource(R.string.item_actions_frozen_meta)
    else -> stringResource(R.string.item_actions_expires, date)
  }
}

/** «dc. 21 oct», localized. */
@Composable
private fun formatDate(date: LocalDate): String = formatDate(date, LocalLocale.current.platformLocale)

private fun formatDate(
  date: LocalDate,
  locale: Locale,
): String = DateTimeFormatter.ofPattern("EEE d MMM", locale).format(date.toJavaLocalDate())

/** − [N de M] +: how many units the buttons below act on (the ones that expire first). */
@Composable
private fun QuantitySelector(
  state: ItemActionsSheetState.Ready,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val container = CaducityTheme.colorScheme.primaryContainer
  val onContainer = CaducityTheme.colorScheme.onPrimaryContainer
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
      StepperButton(
        icon = Symbols.Outlined.Remove,
        description = stringResource(R.string.add_quantity_decrease),
        enabled = state.selected > 1,
        shape = RoundedCornerShape(36.dp, 8.dp, 8.dp, 36.dp),
        container = container,
        content = onContainer,
        onClick = { onSelect(state.selected - 1) },
        modifier = Modifier.weight(1f),
      )
      Surface(
        modifier = Modifier.weight(2.8f).height(72.dp),
        shape = RoundedCornerShape(8.dp),
        color = CaducityTheme.colorScheme.surfaceContainer,
      ) {
        val ofTotal = CaducityTheme.typography.titleMedium
        val ofTotalColor = CaducityTheme.colorScheme.onSurfaceVariant
        val ofTotalText = stringResource(R.string.item_actions_of_total, state.unitCount)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
          // One text, so the number and «de N» share a baseline and the whole thing is centered.
          Text(
            text =
              buildAnnotatedString {
                append(state.selected.toString())
                withStyle(
                  SpanStyle(fontSize = ofTotal.fontSize, fontFamily = ofTotal.fontFamily, color = ofTotalColor),
                ) {
                  append("  $ofTotalText")
                }
              },
            style = CaducityTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
          )
        }
      }
      StepperButton(
        icon = Symbols.Outlined.Add,
        description = stringResource(R.string.add_quantity_increase),
        enabled = state.selected < state.unitCount,
        shape = RoundedCornerShape(8.dp, 36.dp, 36.dp, 8.dp),
        container = container,
        content = onContainer,
        onClick = { onSelect(state.selected + 1) },
        modifier = Modifier.weight(1f),
      )
    }
    Text(
      text =
        if (state.frozenOnly) {
          stringResource(R.string.unfreeze_order)
        } else if (state.expiredCount > 0) {
          stringResource(R.string.item_actions_selector_hint_expired, state.expiredCount)
        } else {
          stringResource(R.string.item_actions_selector_hint)
        },
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun StepperButton(
  icon: ImageVector,
  description: String,
  enabled: Boolean,
  shape: Shape,
  container: Color,
  content: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  FilledIconButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.height(72.dp),
    shape = shape,
    colors =
      IconButtonDefaults.filledIconButtonColors(
        containerColor = container,
        contentColor = content,
        disabledContainerColor = container.copy(alpha = 0.12f),
        disabledContentColor = content.copy(alpha = 0.38f),
      ),
  ) {
    Icon(icon, contentDescription = description, modifier = Modifier.size(28.dp))
  }
}

/** Consumit (wide, `primary`) next to Congela / Descongela (`frozen-container`), 56 dp, joined. */
@Composable
private fun PrimaryActions(
  consumeLabel: String,
  freezeLabel: String,
  freezeIcon: ImageVector,
  showFreeze: Boolean,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val frozen = CaducityTheme.colors.frozen
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
    ActionButton(
      label = consumeLabel,
      icon = Symbols.Outlined.Check,
      container = CaducityTheme.colorScheme.primary,
      content = CaducityTheme.colorScheme.onPrimary,
      shape = if (showFreeze) RoundedCornerShape(32.dp, 8.dp, 8.dp, 32.dp) else RoundedCornerShape(32.dp),
      onClick = onConsume,
      modifier = Modifier.weight(1.4f),
    )
    if (showFreeze) {
      ActionButton(
        label = freezeLabel,
        icon = freezeIcon,
        container = frozen.container,
        content = frozen.onContainer,
        shape = RoundedCornerShape(8.dp, 32.dp, 32.dp, 8.dp),
        onClick = onFreeze,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun ActionButton(
  label: String,
  icon: ImageVector,
  container: Color,
  content: Color,
  shape: Shape,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Button(
    onClick = onClick,
    modifier = modifier.heightIn(min = 56.dp),
    shape = shape,
    colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
    contentPadding = PaddingValues(horizontal = 12.dp),
  ) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
    Spacer(Modifier.width(8.dp))
    Text(label, maxLines = 1)
  }
}

/** «Més accions»: the rest of the actions, folded away until asked for. */
@Composable
private fun MoreActions(
  open: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(if (open) 16.dp else 0.dp)) {
    SegmentedListItem(
      onClick = onToggle,
      shapes = ListItemDefaults.segmentedShapes(0, 1),
      modifier = Modifier.heightIn(min = 56.dp).fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp),
      colors = connectedRowColors(),
      leadingContent = {
        Icon(Symbols.Outlined.MoreHoriz, contentDescription = null, tint = CaducityTheme.colorScheme.onSurfaceVariant)
      },
      trailingContent = {
        Icon(
          if (open) Symbols.Outlined.ExpandLess else Symbols.Outlined.ExpandMore,
          contentDescription = null,
          tint = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      },
    ) {
      Text(text = stringResource(R.string.item_actions_more), style = CaducityTheme.typography.bodyLarge)
    }
    if (open) content()
  }
}

@Composable
private fun ChangeDate(
  onQuick: (QuickReschedule) -> Unit,
  onPick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text = stringResource(R.string.item_actions_change_date).uppercase(),
      style = CaducityTheme.typography.labelSmall,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      QuickDateChips(
        options =
          listOf(
            QuickDateOption(stringResource(R.string.item_actions_plus_1d)),
            QuickDateOption(stringResource(R.string.item_actions_plus_3d)),
            QuickDateOption(stringResource(R.string.item_actions_plus_1w)),
          ),
        onClick = { onQuick(QuickReschedule.entries[it]) },
        modifier = Modifier.weight(1f),
      )
      IconButton(
        onClick = onPick,
        modifier =
          Modifier
            .size(44.dp)
            .clip(CaducityShapes.chip)
            .background(CaducityTheme.colorScheme.surfaceContainerHigh),
      ) {
        Icon(
          Symbols.Outlined.CalendarMonth,
          contentDescription = stringResource(R.string.item_actions_pick_date),
        )
      }
    }
  }
}

@Composable
private fun SecondaryActions(
  actions: ItemActionsHandlers,
  modifier: Modifier = Modifier,
  showTransform: Boolean = true,
) {
  val expired = CaducityTheme.colors.expired.strong
  val count = if (showTransform) 4 else 3
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing)) {
    ActionRow(
      0,
      count,
      Symbols.Outlined.Edit,
      stringResource(R.string.item_actions_edit),
      onClick = actions::onEdit,
    )
    ActionRow(
      1,
      count,
      Symbols.Outlined.DriveFileMove,
      stringResource(R.string.item_actions_move),
      onClick = actions::onMoveClick,
    )
    if (showTransform) {
      ActionRow(
        2,
        count,
        AppIcons.Cooking,
        stringResource(R.string.item_actions_transform),
        containerColor = CaducityTheme.colorScheme.primaryContainer,
        contentColor = CaducityTheme.colorScheme.onPrimaryContainer,
        onClick = actions::onTransform,
      )
    }
    ActionRow(
      count - 1,
      count,
      Symbols.Outlined.Delete,
      stringResource(R.string.item_actions_discard),
      tint = expired,
      onClick = actions::onDiscardAll,
    )
  }
}

@Composable
private fun ActionRow(
  index: Int,
  count: Int,
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = Color.Unspecified,
  containerColor: Color = CaducityTheme.colorScheme.surfaceContainer,
  contentColor: Color = CaducityTheme.colorScheme.onSurface,
) {
  SegmentedListItem(
    onClick = onClick,
    shapes = ListItemDefaults.segmentedShapes(index, count),
    modifier = modifier.heightIn(min = 56.dp),
    contentPadding = PaddingValues(horizontal = 16.dp),
    colors = connectedRowColors(containerColor, contentColor),
    leadingContent = {
      Icon(
        icon,
        contentDescription = null,
        tint =
          tint.takeIf { it != Color.Unspecified } ?: CaducityTheme.colorScheme.onSurfaceVariant,
      )
    },
  ) {
    Text(
      text = label,
      style = CaducityTheme.typography.bodyLarge,
      color = tint.takeIf { it != Color.Unspecified } ?: CaducityTheme.colorScheme.onSurface,
    )
  }
}

/** «Mou»: the other categories, replacing the actions until one is picked. */
@Composable
private fun CategoryPicker(
  categories: List<Category>,
  onPick: (String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .navigationBarsPadding()
        .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
      IconButton(onClick = onBack) {
        Icon(Symbols.Outlined.ArrowBack, contentDescription = stringResource(R.string.item_actions_cancel))
      }
      Text(
        text = stringResource(R.string.item_actions_move_title),
        style = CaducityTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() },
      )
    }
    categories.forEachIndexed { index, category ->
      SegmentedListItem(
        onClick = { onPick(category.id) },
        shapes = ListItemDefaults.segmentedShapes(index, categories.size),
        modifier = Modifier.heightIn(min = 56.dp),
        colors = connectedRowColors(),
        leadingContent = {
          CategoryShapeIcon(shape = category.shape, icon = CategoryIcons.vectorOf(category.icon), size = 36.dp)
        },
      ) {
        Text(category.name, style = CaducityTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SideEffectHandler(
  viewModel: ItemActionsSheetViewModel,
  dialogState: AppDialogState,
  datePickerState: DatePickerState,
  onHaptic: (HapticFeedbackType) -> Unit,
) {
  val okLabel = stringResource(R.string.add_picker_ok)
  val cancelLabel = stringResource(R.string.add_picker_cancel)
  val cancelAction = stringResource(R.string.item_actions_cancel)
  val eatAnywayTitle = stringResource(R.string.item_actions_eat_anyway_title)
  val eatAnywayBody = stringResource(R.string.item_actions_eat_anyway_body)
  val eatAnywayLabel = stringResource(R.string.item_actions_eat_anyway)
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        ItemActionsSheetSideEffect.Confirm -> onHaptic(HapticFeedbackType.Confirm)

        is ItemActionsSheetSideEffect.ShowDatePicker ->
          launch {
            datePickerState.selectedDateMillis = effect.currentMillis
            val result =
              dialogState.showDatePickerDialog(
                datePickerState = datePickerState,
                positiveButton = { Text(okLabel) },
                negativeButton = { Text(cancelLabel) },
                type = AppFeedbackType.Info,
              )
            val picked = datePickerState.selectedDateMillis
            if (result == DialogResult.Positive && picked != null) {
              viewModel.onDatePicked(
                Instant.fromEpochMilliseconds(picked).toLocalDateTime(TimeZone.UTC).date,
                effect.purpose,
              )
            }
          }

        ItemActionsSheetSideEffect.ShowEatAnywayConfirm ->
          launch {
            val result =
              dialogState.showAlertDialog(
                title = { Text(eatAnywayTitle) },
                content = { Text(eatAnywayBody) },
                positiveButton = { Text(eatAnywayLabel) },
                negativeButton = { Text(cancelAction) },
                type = AppFeedbackType.Error,
              )
            if (result == DialogResult.Positive) viewModel.onEatAnyway()
          }
      }
    }
  }
}
