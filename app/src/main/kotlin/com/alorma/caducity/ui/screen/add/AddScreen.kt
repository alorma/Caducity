package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.bottomsheet.rememberAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.threshold.thresholdLabel
import com.alorma.caducity.ui.screen.category.editor.CategoryDurationSheet
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
  // Skips the half-open stop so the Durada sheet shows its «Fet» button without dragging.
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
        // Editing a unit has no quantity nor frozen toggle: its name, category, date and the product's type can change.
        // «Com ho comptes?» and «Es pot congelar» apply to the whole product.
        val canFreeze = state.canFreeze
        val category = state.category
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            UnitTypeRow(
              unitType = state.unitType,
              onClick = viewModel::onUnitTypeClick,
              shapePosition = if (state.isEditing) ShapePosition.Single else ShapePosition.Start,
            )
            if (state.resetsQuantity) {
              Text(
                text = stringResource(R.string.unit_type_change_warning),
                style = CaducityTheme.typography.bodySmall,
                color = CaducityTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
            if (!state.isEditing && state.isHandful) {
              LevelFrozenRow(
                level = state.level,
                frozen = state.frozenEffective,
                onLevelChange = viewModel::onLevelChange,
                onFrozenChange = viewModel::onFrozenChange,
                canFreeze = canFreeze != false,
              )
            } else if (!state.isEditing) {
              QuantityFrozenRow(
                quantity = state.quantity,
                unitType = state.unitType,
                amount = state.amount,
                onAmountChange = viewModel::onAmountChange,
                frozen = state.frozenEffective,
                onQuantityChange = viewModel::onQuantityChange,
                onFrozenChange = viewModel::onFrozenChange,
                canFreeze = canFreeze != false,
              )
            }
          }
          if (!state.isEditing && state.isMeasured) {
            AmountChips(unitType = state.unitType, amount = state.amount, onSelect = viewModel::onAmountChange)
          }
          if (canFreeze != null && category != null) {
            CanFreezeCard(
              canFreeze = canFreeze,
              categoryName = category.name,
              categoryCanFreeze = category.canFreeze,
              onCanFreezeChange = viewModel::onCanFreezeChange,
            )
          }
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
      Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = viewModel::onChangeSuggestedDuration),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = stringResource(R.string.add_date_suggested, categoryName, thresholdLabel(suggestedDays)),
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f),
        )
        Text(
          text = stringResource(R.string.add_date_suggested_change),
          style = CaducityTheme.typography.labelLarge,
          color = CaducityTheme.colorScheme.primary,
          modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        )
      }
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
