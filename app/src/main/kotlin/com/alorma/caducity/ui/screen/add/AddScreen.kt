package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.rememberAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.threshold.thresholdLabel
import com.alorma.caducity.ui.theme.CaducityTheme
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
  val bottomSheetState = rememberAppBottomSheetState()
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
    datePickerState = datePickerState,
    onHaptic = { haptics.performHapticFeedback(it) },
  )

  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
    topBar = {
      AddTopBar(editing = state.isEditing, onClose = {
        viewModel.navigate(AddNavigation.Close)
      })
    },
    bottomBar = {
      // The bar rides above the keyboard and the navigation bar; the scaffold shrinks the content for it.
      ResponsiveCenteredContainer(fillHeight = false) {
        AddBottomBar(
          state = state,
          onSave = viewModel::onSave,
          modifier =
            Modifier
              .imePadding()
              .navigationBarsPadding()
              .padding(horizontal = 20.dp)
              .padding(top = 8.dp, bottom = 16.dp),
        )
      }
    },
  ) { insets ->
    ResponsiveCenteredContainer {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(insets)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenGutter),
        verticalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        NameSection(state = state, viewModel = viewModel, focusRequester = focusRequester)
        CategorySection(state = state, onSelect = viewModel::onCategorySelect, onNew = {
          viewModel.navigate(AddNavigation.QuickCategory)
        })
        DateSection(state = state, viewModel = viewModel)
        // Editing a unit has no quantity nor frozen toggle: only its name, category and date can change.
        if (!state.isEditing) {
          QuantityFrozenRow(
            quantity = state.quantity,
            frozen = state.frozen,
            onQuantityChange = viewModel::onQuantityChange,
            onFrozenChange = viewModel::onFrozenChange,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTopBar(
  editing: Boolean,
  onClose: () -> Unit,
) {
  ResponsiveCenteredContainer(fillHeight = false) {
    TopAppBar(
      title = {
        Text(
          text = stringResource(if (editing) R.string.add_edit_title else R.string.add_title),
          style = CaducityTheme.typography.titleLarge,
        )
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
private fun NameSection(
  state: AddState,
  viewModel: AddViewModel,
  focusRequester: FocusRequester,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AddOverline(stringResource(R.string.add_name_label))
    NameField(
      name = state.name,
      onNameChange = viewModel::onNameChange,
      onClear = viewModel::onClearName,
      focusRequester = focusRequester,
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

      state.isNewProduct -> NewProductNotice()
    }
  }
}

@Composable
private fun CategorySection(
  state: AddState,
  onSelect: (String) -> Unit,
  onNew: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AddOverline(stringResource(R.string.add_category_label))
    CategoryChips(
      categories = state.categories,
      selectedId = state.categoryId,
      highlightNew = state.isNewProduct && state.categoryId == null,
      onSelect = onSelect,
      onNew = onNew,
    )
  }
}

@Composable
private fun DateSection(
  state: AddState,
  viewModel: AddViewModel,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AddOverline(stringResource(R.string.add_date_label))
    ExpiryDateField(
      digits = state.dateDigits,
      date = state.date,
      today = state.today,
      onDigitsChange = viewModel::onDateTextChange,
      onPickerClick = viewModel::onPickDate,
    )
    QuickDateRow(
      onSelect = viewModel::onQuickDate,
      modifier = Modifier.fullBleed(ScreenGutter),
      contentPadding = PaddingValues(horizontal = ScreenGutter),
      date = state.date,
      today = state.today,
    )
    val suggestedDays = state.suggestedDurationDays
    val categoryName = state.categories.firstOrNull { it.id == state.categoryId }?.name
    if (suggestedDays != null && categoryName != null) {
      Text(
        text = stringResource(R.string.add_date_suggested, categoryName, thresholdLabel(suggestedDays)),
        style = CaducityTheme.typography.bodySmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun SideEffectHandler(
  viewModel: AddViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  datePickerState: DatePickerState,
  onHaptic: (HapticFeedbackType) -> Unit,
) {
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        AddSideEffect.Confirm -> onHaptic(HapticFeedbackType.Confirm)
        AddSideEffect.Reject -> onHaptic(HapticFeedbackType.Reject)

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
