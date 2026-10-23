package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.bottomsheet.rememberAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.category.editor.CategoryDurationSheet
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddScreen(
  args: AddArgs,
  onClose: () -> Unit,
  onOpenQuickCategory: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AddViewModel = koinViewModel { parametersOf(args) },
) {
  val state by viewModel.state.collectAsState()
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()
  // Skips the half-open stop so the sheets show their «Fet» button without dragging.
  val bottomSheetState = rememberAppBottomSheetState(skipHalfExpanded = true)
  val datePickerState = rememberDatePickerState()
  val focusRequester = remember { FocusRequester() }
  val haptics = LocalHapticFeedback.current

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        AddNavigationSideEffect.Close -> onClose()
        AddNavigationSideEffect.OpenQuickCategory -> onOpenQuickCategory()
      }
    }
  }
  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  SideEffectHandler(
    viewModel = viewModel,
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
    datePickerState = datePickerState,
    onHaptic = { haptics.performHapticFeedback(it) },
  )

  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
    showScreenResults = false,
    topBar = {
      StyledTopAppBar(
        title = { Text(stringResource(if (state.isEditing) R.string.add_edit_title else R.string.add_title)) },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigate(AddNavigation.Close) }) {
            Icon(Symbols.Outlined.Close, contentDescription = stringResource(R.string.add_close))
          }
        },
        actions = {
          if (state.isEditing) {
            IconButton(onClick = viewModel::onDeleteClick) {
              Icon(
                imageVector = Symbols.Outlined.Delete,
                contentDescription = stringResource(R.string.add_delete),
                tint = CaducityTheme.colors.expired.strong,
              )
            }
          }
          Button(
            onClick = viewModel::onSave,
            enabled = state.canSave,
            modifier = Modifier.padding(end = 8.dp).heightIn(min = 44.dp),
          ) {
            Text(stringResource(R.string.add_save), maxLines = 1)
          }
        },
      )
    },
  ) { insets ->
    ResponsiveCenteredContainer {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(insets)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenGutter)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        NameSection(state = state, viewModel = viewModel, focusRequester = focusRequester)
        AddOptionsList(
          state = state,
          onCategoryClick = viewModel::onCategoryClick,
          onDateClick = viewModel::onDateClick,
          onQuantityClick = viewModel::onQuantityClick,
          onCanFreezeChange = viewModel::onCanFreezeChange,
          onFrozenChange = viewModel::onFrozenChange,
        )
        if (state.isEditing) AddEditNote(frozen = state.frozen)
        AddMissingNote(missing = state.missing)
      }
    }
  }
}

@Composable
private fun NameSection(
  state: AddState,
  viewModel: AddViewModel,
  focusRequester: FocusRequester,
) {
  val focusManager = LocalFocusManager.current
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AddOverline(stringResource(R.string.add_name_label))
    NameField(
      name = state.name,
      onNameChange = viewModel::onNameChange,
      onClear = viewModel::onClearName,
      focusRequester = focusRequester,
      onSubmit = {
        focusManager.clearFocus()
        viewModel.onNameSubmit()
      },
    )
    val suggestion = state.suggestion
    when {
      suggestion != null ->
        SuggestionRow(
          suggestion = suggestion.name,
          typed = state.name,
          categoryName = state.categories.firstOrNull { it.id == suggestion.categoryId }?.name,
          onClick = viewModel::onSuggestionClick,
        )

      !state.isEditing && state.name.isNotBlank() && (state.fromHistory || state.isNewProduct) ->
        AddStatusLine(fromHistory = state.fromHistory)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun SideEffectHandler(
  viewModel: AddViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  bottomSheetState: AppBottomSheetState,
  datePickerState: DatePickerState,
  onHaptic: (HapticFeedbackType) -> Unit,
) {
  val scope = rememberCoroutineScope()
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        AddSideEffect.Confirm -> onHaptic(HapticFeedbackType.Confirm)
        AddSideEffect.Reject -> onHaptic(HapticFeedbackType.Reject)

        AddSideEffect.ShowCategorySheet ->
          bottomSheetState.show(onDismissRequest = viewModel::onGuidedFlowDismissed) {
            val current by viewModel.state.collectAsState()
            CategorySheet(
              state = current,
              onSelect = { id ->
                // In the guided flow the «Caduca» sheet takes this one's place, so it stays open.
                if (!viewModel.onCategorySelect(id)) scope.launch { bottomSheetState.hide() }
              },
              onNew = {
                scope.launch { bottomSheetState.hide() }
                viewModel.navigate(AddNavigation.QuickCategory)
              },
            )
          }

        AddSideEffect.ShowDateSheet ->
          bottomSheetState.show {
            val current by viewModel.state.collectAsState()
            DateSheet(
              state = current,
              onDigitsChange = viewModel::onDateTextChange,
              onPickerClick = viewModel::onPickDate,
              onQuickDate = viewModel::onQuickDate,
              onChangeSuggestedDuration = viewModel::onChangeSuggestedDuration,
              onDone = { scope.launch { bottomSheetState.hide() } },
            )
          }

        AddSideEffect.ShowQuantitySheet ->
          bottomSheetState.show {
            val current by viewModel.state.collectAsState()
            QuantitySheet(
              state = current,
              onUnitTypeClick = viewModel::onUnitTypeClick,
              onUnitTypeChange = viewModel::onUnitTypeChange,
              onQuantityChange = viewModel::onQuantityChange,
              onAmountChange = viewModel::onAmountChange,
              onLevelChange = viewModel::onLevelChange,
              onDone = { scope.launch { bottomSheetState.hide() } },
            )
          }

        is AddSideEffect.ShowUnitTypePicker ->
          bottomSheetState.show {
            UnitTypeSheet(
              selected = effect.current,
              onSelect = { type ->
                viewModel.onUnitTypeChange(type)
                scope.launch { bottomSheetState.hide() }
              },
            )
          }

        is AddSideEffect.ShowCategoryDurationSheet ->
          bottomSheetState.show {
            val current by viewModel.state.collectAsState()
            CategoryDurationSheet(
              categoryName = effect.categoryName,
              usualDurationDays = current.category?.usualDurationDays,
              onDurationChange = viewModel::onCategoryDurationChange,
              onDone = { scope.launch { bottomSheetState.hide() } },
            )
          }

        AddSideEffect.ShowSaveFailed ->
          launch {
            snackbarState.showSnackbar(message = R.string.add_save_failed, type = AppFeedbackType.Error)
          }

        AddSideEffect.ShowFrozenPastDate ->
          launch {
            snackbarState.showSnackbar(message = R.string.add_frozen_past_date, type = AppFeedbackType.Error)
          }

        is AddSideEffect.ShowDatePicker ->
          launch {
            datePickerState.selectedDateMillis = effect.currentMillis
            val result =
              dialogState.showDatePickerDialog(
                datePickerState = datePickerState,
                positiveButton = { Text(stringResource(R.string.add_picker_ok)) },
                negativeButton = { Text(stringResource(R.string.add_picker_cancel)) },
                type = AppFeedbackType.Info,
              )
            val picked = datePickerState.selectedDateMillis
            if (result == DialogResult.Positive && picked != null) {
              viewModel.onDatePicked(Instant.fromEpochMilliseconds(picked).toLocalDateTime(TimeZone.UTC).date)
            }
          }
      }
    }
  }
}

internal val ScreenGutter = 20.dp

/** Lets a scrolling row run to the edges of the container, ignoring the horizontal [gutter] of its parent. */
internal fun Modifier.fullBleed(gutter: Dp): Modifier =
  layout { measurable, constraints ->
    val extra = gutter.roundToPx() * 2
    val placeable =
      measurable.measure(
        constraints.copy(minWidth = constraints.maxWidth + extra, maxWidth = constraints.maxWidth + extra),
      )
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
  }
