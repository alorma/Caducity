package com.alorma.caducity.ui.components.actions

import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.ArrowBack
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.DriveFileMove
import com.alorma.caducity.base.ui.icons.symbols.Edit
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.config.remoteconfig.rememberRemoteConfig
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.ui.components.bottomsheet.QuantityDialogData
import com.alorma.caducity.ui.components.bottomsheet.QuantitySelectorResult
import com.alorma.caducity.ui.components.bottomsheet.showQuantitySelectorDialog
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.chips.QuickDateChips
import com.alorma.caducity.ui.components.chips.QuickDateOption
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogHost
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.status.StatusPill
import com.alorma.caducity.ui.components.status.statusPillText
import com.alorma.caducity.ui.screen.transform.TransformFeature
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

/** Content of the actions bottom sheet (`ItemActions` route): everything you can do to one unit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemActionsSheet(
  itemId: String,
  onClose: () -> Unit,
  onEdit: (itemId: String) -> Unit,
  onTransform: (itemId: String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ItemActionsSheetViewModel = koinViewModel { parametersOf(itemId) },
) {
  val state by viewModel.state.collectAsState()
  val dialogState = rememberAppDialogState()
  val datePickerState = rememberDatePickerState()
  val haptics = LocalHapticFeedback.current
  val transformEnabled = rememberRemoteConfig<TransformFeature>().isEnabled()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        ItemActionsSheetNavigationSideEffect.Close -> onClose()
        is ItemActionsSheetNavigationSideEffect.Edit -> onEdit(effect.itemId)
        is ItemActionsSheetNavigationSideEffect.Transform -> onTransform(effect.itemId)
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
          ActionsContent(state = current, viewModel = viewModel, transformEnabled = transformEnabled)
        }
    }
    AppDialogHost(dialogState)
  }
}

@Composable
private fun ActionsContent(
  state: ItemActionsSheetState.Ready,
  viewModel: ItemActionsSheetViewModel,
  transformEnabled: Boolean,
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
    PrimaryActions(state = state, onConsume = viewModel::onConsume, onFreeze = viewModel::onFreezeToggle)
    if (!state.frozen) {
      ChangeDate(onQuick = viewModel::onQuickReschedule, onPick = viewModel::onPickDate)
    }
    SecondaryActions(
      onEdit = { viewModel.navigate(ItemActionsSheetNavigation.Edit) },
      onMove = viewModel::onMoveClick,
      onTransform = if (transformEnabled) ({ viewModel.navigate(ItemActionsSheetNavigation.Transform) }) else null,
      onDelete = viewModel::onDelete,
    )
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
    StatusPill(status = state.status, label = statusPillText(state.status, state.daysLeft))
  }
}

@Composable
private fun metaText(state: ItemActionsSheetState.Ready): String {
  val date = formatDate(state.expiration)
  return when {
    state.pack != null -> stringResource(R.string.item_actions_pack, state.pack, date)
    state.frozen && state.unitCount > 1 ->
      stringResource(R.string.item_actions_unit_of_frozen, state.unitIndex, state.unitCount)
    state.frozen -> stringResource(R.string.item_actions_frozen_meta)
    state.unitCount > 1 -> stringResource(R.string.item_actions_unit_of, state.unitIndex, state.unitCount, date)
    else -> stringResource(R.string.item_actions_expires, date)
  }
}

/** «caduca demà», «caduca dc. 21 oct» or «ja ha caducat», lowercase, for the quantity dialog. */
private fun quantityExpiresText(
  resources: Resources,
  locale: Locale,
  daysLeft: Int,
  date: LocalDate,
): String =
  when {
    daysLeft < 0 -> resources.getString(R.string.quantity_expired)
    daysLeft == 0 -> resources.getString(R.string.quantity_expires_today)
    daysLeft == 1 -> resources.getString(R.string.quantity_expires_tomorrow)
    else -> resources.getString(R.string.quantity_expires_on, formatDate(date, locale))
  }

/** «dc. 21 oct», localized. */
@Composable
private fun formatDate(date: LocalDate): String = formatDate(date, LocalLocale.current.platformLocale)

private fun formatDate(
  date: LocalDate,
  locale: Locale,
): String = DateTimeFormatter.ofPattern("EEE d MMM", locale).format(date.toJavaLocalDate())

/** Consumit (wide, `primary`) next to Congela / Descongela (`frozen-container`), 64 dp, joined. */
@Composable
private fun PrimaryActions(
  state: ItemActionsSheetState.Ready,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val frozen = CaducityTheme.colors.frozen
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
    ActionButton(
      label = stringResource(R.string.item_actions_consumed),
      icon = Symbols.Outlined.Check,
      container = CaducityTheme.colorScheme.primary,
      content = CaducityTheme.colorScheme.onPrimary,
      shape = RoundedCornerShape(32.dp, 8.dp, 8.dp, 32.dp),
      onClick = onConsume,
      modifier = Modifier.weight(1.4f),
    )
    ActionButton(
      label =
        stringResource(if (state.frozen) R.string.item_actions_unfreeze else R.string.item_actions_freeze),
      icon = Symbols.Outlined.AcUnit,
      container = frozen.container,
      content = frozen.onContainer,
      shape = RoundedCornerShape(8.dp, 32.dp, 32.dp, 8.dp),
      onClick = onFreeze,
      modifier = Modifier.weight(1f),
    )
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
  Surface(
    onClick = onClick,
    modifier = modifier.height(64.dp),
    shape = shape,
    color = container,
    contentColor = content,
  ) {
    Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
      Spacer(Modifier.width(8.dp))
      Text(label, style = CaducityTheme.typography.labelLarge, maxLines = 1)
    }
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
  onEdit: () -> Unit,
  onMove: () -> Unit,
  onTransform: (() -> Unit)?,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val expired = CaducityTheme.colors.expired.strong
  val count = if (onTransform != null) 4 else 3
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing)) {
    ActionRow(0, count, Symbols.Outlined.Edit, stringResource(R.string.item_actions_edit), onClick = onEdit)
    ActionRow(1, count, Symbols.Outlined.DriveFileMove, stringResource(R.string.item_actions_move), onClick = onMove)
    if (onTransform != null) {
      ActionRow(
        2,
        count,
        AppIcons.Cooking,
        stringResource(R.string.item_actions_transform),
        containerColor = CaducityTheme.colorScheme.primaryContainer,
        contentColor = CaducityTheme.colorScheme.onPrimaryContainer,
        onClick = onTransform,
      )
    }
    ActionRow(
      count - 1,
      count,
      Symbols.Outlined.Delete,
      stringResource(R.string.item_actions_delete),
      tint = expired,
      onClick = onDelete,
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
  ConnectedRow(
    index = index,
    count = count,
    onClick = onClick,
    modifier = modifier.height(56.dp),
    contentPadding = PaddingValues(horizontal = 16.dp),
    containerColor = containerColor,
    contentColor = contentColor,
  ) {
    Icon(
      icon,
      contentDescription = null,
      tint =
        tint.takeIf { it != Color.Unspecified } ?: CaducityTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.width(16.dp))
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
      ConnectedRow(
        index = index,
        count = categories.size,
        onClick = { onPick(category.id) },
        modifier = Modifier.height(56.dp),
      ) {
        CategoryShapeIcon(shape = category.shape, icon = CategoryIcons.vectorOf(category.icon), size = 36.dp)
        Spacer(Modifier.width(14.dp))
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
  val deleteLabel = stringResource(R.string.item_actions_delete)
  val cancelAction = stringResource(R.string.item_actions_cancel)
  val resources = LocalResources.current
  val locale = LocalLocale.current.platformLocale
  val deleteBody = stringResource(R.string.item_actions_delete_body)
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
              viewModel.onDatePicked(Instant.fromEpochMilliseconds(picked).toLocalDateTime(TimeZone.UTC).date)
            }
          }

        is ItemActionsSheetSideEffect.ShowDeleteConfirm ->
          launch {
            val result =
              dialogState.showAlertDialog(
                title = { Text(stringResource(R.string.item_actions_delete_title, effect.name)) },
                content = { Text(deleteBody) },
                positiveButton = { Text(deleteLabel) },
                negativeButton = { Text(cancelAction) },
                type = AppFeedbackType.Error,
              )
            if (result == DialogResult.Positive) viewModel.onDeleteConfirmed()
          }

        is ItemActionsSheetSideEffect.ShowQuantitySelector ->
          launch {
            val result =
              dialogState.showQuantitySelectorDialog(
                QuantityDialogData(
                  name = effect.name,
                  shape = effect.category.shape,
                  icon = CategoryIcons.vectorOf(effect.category.icon),
                  expires = quantityExpiresText(resources, locale, effect.daysLeft, effect.expiration),
                  max = effect.max,
                ),
              )
            if (result is QuantitySelectorResult.Selected) viewModel.onQuantitySelected(result.quantity)
          }
      }
    }
  }
}
