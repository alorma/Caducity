package com.alorma.caducity.ui.screen.transform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarResult
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.screen.add.ScreenGutter
import com.alorma.caducity.ui.screen.add.UnitTypeSheet
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
  val links = remember { RowLinks() }
  val actions =
    remember(viewModel) {
      ResultActions(
        onSelect = viewModel::onSelectResult,
        onDone = viewModel::onDone,
        onRemove = viewModel::onRemoveResult,
        onAdd = viewModel::onAddResult,
        onNameChange = viewModel::onNameChange,
        onSuggestionClick = viewModel::onSuggestionClick,
        onServingsChange = viewModel::onServingsChange,
        onCategorySelect = viewModel::onCategorySelect,
        onPickDate = viewModel::onPickDate,
        onFrozenCountChange = viewModel::onFrozenCountChange,
        onUnitTypeClick = viewModel::onUnitTypeClick,
        onAmountChange = viewModel::onAmountChange,
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
    bottomSheetState = bottomSheetState,
  )

  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
    topBar = { TransformTopBar(onClose = viewModel::onCloseRequest) },
    // In the bottom bar slot, so the snackbar shows above «Desa» instead of covering it.
    bottomBar = {
      ResponsiveCenteredContainer(fillHeight = false) {
        TransformSaveBar(
          canSave = state.canSave,
          count = state.results.size,
          onSave = viewModel::onSave,
          modifier = Modifier.navigationBarsPadding(),
        )
      }
    },
  ) { insets ->
    ResponsiveCenteredContainer {
      Box(modifier = Modifier.fillMaxSize().padding(insets)) {
        Column(
          modifier =
            Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(bottom = 16.dp)
              .rowLinks(links, expanded = state.results.any { it.id == state.expandedResultId }),
          verticalArrangement = Arrangement.spacedBy(SectionSpacing),
        ) {
          IngredientsSection(
            ingredients = state.ingredients,
            categories = state.categories,
            onCountChange = viewModel::onIngredientCountChange,
            onAmountChange = viewModel::onIngredientAmountChange,
            onRemove = viewModel::onRemoveIngredient,
            onAdd = { viewModel.navigate(TransformNavigation.IngredientPicker) },
            optional = state.freeEntry,
            closedRows = state.closedRows,
            links = links,
          )
          ResultsSection(state = state, actions = actions, links = links)
          if (state.ingredients.isNotEmpty()) {
            val footerTitle = stringResource(R.string.transform_footer_title)
            val footer = stringResource(R.string.transform_footer)
            Text(
              text =
                buildAnnotatedString {
                  withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(footerTitle) }
                  append(" ")
                  append(footer)
                },
              style = CaducityTheme.typography.bodySmall,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = ScreenGutter + 4.dp, vertical = 8.dp),
            )
          }
        }
      }
    }
  }
}

/** «Desa · M productes»: fixed at the bottom, over the keyboard. */
@Composable
internal fun TransformSaveBar(
  canSave: Boolean,
  count: Int,
  onSave: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .imePadding()
        .padding(start = ScreenGutter, end = ScreenGutter, top = 8.dp, bottom = 12.dp),
  ) {
    Button(
      onClick = onSave,
      enabled = canSave,
      modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
      Text(
        // Disabled, it only says «Desa»: the count of products comes once there is something to save.
        text =
          if (canSave) {
            pluralStringResource(R.plurals.transform_save_count, count, count)
          } else {
            stringResource(R.string.add_save)
          },
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransformTopBar(onClose: () -> Unit) {
  ResponsiveCenteredContainer(fillHeight = false) {
    TopAppBar(
      title = { Text(text = stringResource(R.string.transform_title), style = CaducityTheme.typography.titleLarge) },
      navigationIcon = {
        IconButton(onClick = onClose) {
          Icon(Symbols.Outlined.Close, contentDescription = stringResource(R.string.add_close))
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
  bottomSheetState: AppBottomSheetState,
) {
  val scope = rememberCoroutineScope()
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
                type = AppFeedbackType.Info,
              )
            if (result == DialogResult.Positive) viewModel.onDiscardConfirmed()
          }

        TransformSideEffect.ShowResultRemoved ->
          launch {
            val result =
              snackbarState.showSnackbar(
                message = R.string.transform_result_removed,
                actionLabel = R.string.add_undo,
                type = AppFeedbackType.Info,
              )
            if (result == AppSnackbarResult.ActionPerformed) viewModel.onUndoRemoveResult()
          }

        TransformSideEffect.ShowSaveFailed ->
          launch { snackbarState.showSnackbar(message = R.string.add_save_failed, type = AppFeedbackType.Error) }

        TransformSideEffect.ShowFrozenPastDate ->
          launch { snackbarState.showSnackbar(message = R.string.add_frozen_past_date, type = AppFeedbackType.Error) }

        is TransformSideEffect.ShowUnitTypePicker ->
          bottomSheetState.show {
            UnitTypeSheet(
              selected = effect.current,
              onSelect = { type ->
                viewModel.onUnitTypeChange(effect.resultId, type)
                scope.launch { bottomSheetState.hide() }
              },
            )
          }

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
