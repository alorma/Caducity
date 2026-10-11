package com.alorma.caducity.ui.screen.category.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.feature.tracking.CategoryEditorScreen as CategoryEditorScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.FormOverline
import com.alorma.caducity.ui.components.category.IconPicker
import com.alorma.caducity.ui.components.category.ShapePicker
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.threshold.ThresholdSlider
import com.alorma.caducity.ui.components.threshold.thresholdLabel
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CategoryEditorScreen(
  categoryId: String?,
  onClose: () -> Unit,
  onCreated: (categoryId: String) -> Unit,
  onDeleted: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CategoryEditorViewModel = koinViewModel { parametersOf(categoryId) },
) {
  TrackScreen(screen = CategoryEditorScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        CategoryEditorNavigationSideEffect.Close -> onClose()
        is CategoryEditorNavigationSideEffect.Created -> onCreated(effect.categoryId)
        CategoryEditorNavigationSideEffect.Deleted -> onDeleted()
      }
    }
  }
  SideEffectHandler(viewModel, dialogState, snackbarState)

  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    topBar = {
      StyledTopAppBar(
        title = {
          Text(
            stringResource(
              if (state.isEditing) R.string.category_editor_title_edit else R.string.category_editor_title_new,
            ),
          )
        },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigate(CategoryEditorNavigation.Close) }) {
            Icon(Symbols.Outlined.Close, contentDescription = stringResource(R.string.category_editor_close))
          }
        },
        actions = {
          Button(
            onClick = viewModel::onSave,
            enabled = state.canSave,
            modifier = Modifier.padding(end = 8.dp).heightIn(min = 44.dp),
          ) {
            Text(stringResource(R.string.category_editor_save))
          }
        },
      )
    },
  ) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      CategoryEditorContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onShapeChange = viewModel::onShapeChange,
        onIconChange = viewModel::onIconChange,
        onSoonDaysChange = viewModel::onSoonDaysChange,
        onDelete = viewModel::onDeleteClick,
      )
    }
  }
}

@Composable
private fun CategoryEditorContent(
  state: CategoryEditorState,
  onNameChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onShapeChange: (CategoryShape) -> Unit,
  onIconChange: (CategoryIcon) -> Unit,
  onSoonDaysChange: (Int) -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
        .padding(bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      CategoryShapeIcon(shape = state.shape, icon = state.icon.imageVector, size = 96.dp, selected = true)
      Text(
        text = state.name.ifBlank { stringResource(R.string.category_editor_preview_placeholder) },
        style = CaducityTheme.typography.headlineSmall,
        color =
          if (state.name.isBlank()) CaducityTheme.colorScheme.onSurfaceVariant else CaducityTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 2,
      )
    }

    OutlinedTextField(
      value = state.name,
      onValueChange = onNameChange,
      label = { Text(stringResource(R.string.category_form_name)) },
      singleLine = true,
      isError = state.error != null,
      supportingText =
        state.error?.let {
          {
            Text(
              stringResource(
                if (it == CategoryEditorError.Duplicate) {
                  R.string.quick_category_error_duplicate
                } else {
                  R.string.category_editor_error_failed
                },
              ),
            )
          }
        },
      keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
      shape = CaducityShapes.field,
      modifier = Modifier.fillMaxWidth(),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      FormOverline(stringResource(R.string.category_form_shape))
      ShapePicker(selected = state.shape, onSelect = onShapeChange)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      FormOverline(stringResource(R.string.category_form_icon))
      IconPicker(selected = state.icon, onSelect = onIconChange)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      FormOverline(stringResource(R.string.category_editor_margin))
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = CaducityTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          ThresholdSlider(days = state.shownSoonDays, onDaysChange = onSoonDaysChange)
          Text(
            text =
              stringResource(
                R.string.category_editor_margin_help,
                state.name.trim().ifBlank { stringResource(R.string.category_editor_margin_this_category) },
                thresholdLabel(state.shownSoonDays),
              ),
            style = CaducityTheme.typography.bodySmall,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      FormOverline(stringResource(R.string.category_editor_description))
      OutlinedTextField(
        value = state.description,
        onValueChange = onDescriptionChange,
        placeholder = { Text(stringResource(R.string.category_editor_description_placeholder)) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        shape = CaducityShapes.field,
        minLines = 3,
        maxLines = 5,
        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
      )
    }

    if (state.isEditing) {
      val expired = CaducityTheme.colors.expired
      OutlinedButton(
        onClick = onDelete,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = expired.strong),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Symbols.Outlined.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        Text(stringResource(R.string.category_editor_delete))
      }
    }
  }
}

@Composable
private fun SideEffectHandler(
  viewModel: CategoryEditorViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
) {
  val scope = rememberCoroutineScope()
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is CategoryEditorSideEffect.ShowDeleteConfirm ->
          scope.launch {
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
        CategoryEditorSideEffect.DeleteFailed ->
          scope.launch {
            snackbarState.showSnackbar(message = R.string.error_delete_category_failed, type = AppFeedbackType.Error)
          }
      }
    }
  }
}
