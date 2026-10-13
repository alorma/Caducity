package com.alorma.caducity.ui.screen.transform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import com.alorma.caducity.ui.screen.add.ScreenGutter
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TransformScreen(
  args: TransformArgs,
  onClose: () -> Unit,
  onOpenIngredientPicker: (selectedItemIds: List<String>) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: TransformViewModel = koinViewModel { parametersOf(args) },
) {
  val state by viewModel.state.collectAsState()
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()
  val bottomSheetState = rememberAppBottomSheetState()
  val datePickerState = rememberDatePickerState()
  val actions =
    remember(viewModel) {
      ResultActions(
        onToggle = viewModel::onToggleResult,
        onRemove = viewModel::onRemoveResult,
        onAdd = viewModel::onAddResult,
        onNameChange = viewModel::onNameChange,
        onSuggestionClick = viewModel::onSuggestionClick,
        onServingsChange = viewModel::onServingsChange,
        onCategorySelect = viewModel::onCategorySelect,
        onPickDate = viewModel::onPickDate,
        onFrozenCountChange = viewModel::onFrozenCountChange,
      )
    }

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        TransformNavigationSideEffect.Close -> onClose()
        is TransformNavigationSideEffect.OpenIngredientPicker -> onOpenIngredientPicker(effect.selectedItemIds)
      }
    }
  }

  SideEffectHandler(
    viewModel = viewModel,
    dialogState = dialogState,
    snackbarState = snackbarState,
    datePickerState = datePickerState,
  )

  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
    topBar = {
      TransformTopBar(
        canSave = state.canSave,
        onClose = viewModel::onCloseRequest,
        onSave = viewModel::onSave,
      )
    },
  ) { insets ->
    ResponsiveCenteredContainer {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(insets)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        IngredientsSection(
          ingredients = state.ingredients,
          categories = state.categories,
          onCountChange = viewModel::onIngredientCountChange,
          onRemove = viewModel::onRemoveIngredient,
          onAdd = { viewModel.navigate(TransformNavigation.IngredientPicker) },
          optional = state.freeEntry,
          closedRows = state.closedRows,
        )
        ResultsSection(state = state, actions = actions, modifier = Modifier.padding(horizontal = ScreenGutter))
        Text(
          text =
            stringResource(
              if (state.ingredients.isEmpty()) R.string.transform_footer_free else R.string.transform_footer,
            ),
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = ScreenGutter + 4.dp, vertical = 8.dp),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransformTopBar(
  canSave: Boolean,
  onClose: () -> Unit,
  onSave: () -> Unit,
) {
  ResponsiveCenteredContainer(fillHeight = false) {
    TopAppBar(
      title = { Text(text = stringResource(R.string.transform_title), style = CaducityTheme.typography.titleLarge) },
      navigationIcon = {
        IconButton(onClick = onClose) {
          Icon(Symbols.Outlined.Close, contentDescription = stringResource(R.string.add_close))
        }
      },
      actions = {
        Surface(
          onClick = onSave,
          enabled = canSave,
          shape = CaducityShapes.full,
          color = if (canSave) CaducityTheme.colorScheme.primary else CaducityTheme.colorScheme.surfaceContainerHigh,
          contentColor = if (canSave) CaducityTheme.colorScheme.onPrimary else CaducityTheme.colorScheme.outline,
          modifier = Modifier.padding(end = 12.dp),
        ) {
          Text(
            text = stringResource(R.string.transform_save),
            style = CaducityTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun SideEffectHandler(
  viewModel: TransformViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  datePickerState: DatePickerState,
) {
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        TransformSideEffect.ShowDiscardConfirm ->
          launch {
            val result =
              dialogState.showAlertDialog(
                title = { Text(stringResource(R.string.transform_discard_title)) },
                content = { Text(stringResource(R.string.transform_discard_message)) },
                positiveButton = { Text(stringResource(R.string.transform_discard_confirm)) },
                negativeButton = { Text(stringResource(R.string.transform_discard_keep)) },
                type = AppFeedbackType.Error,
              )
            if (result == DialogResult.Positive) viewModel.onDiscardConfirmed()
          }

        TransformSideEffect.ShowSaveFailed ->
          launch { snackbarState.showSnackbar(message = R.string.add_save_failed, type = AppFeedbackType.Error) }

        TransformSideEffect.ShowFrozenPastDate ->
          launch { snackbarState.showSnackbar(message = R.string.add_frozen_past_date, type = AppFeedbackType.Error) }

        is TransformSideEffect.ShowDatePicker ->
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
              val date = Instant.fromEpochMilliseconds(picked).toLocalDateTime(TimeZone.UTC).date
              viewModel.onDatePicked(effect.resultId, date)
            }
          }
      }
    }
  }
}
