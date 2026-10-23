package com.alorma.caducity.ui.screen.category.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.ChevronRight
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.Edit
import com.alorma.caducity.base.ui.icons.symbols.EventAvailable
import com.alorma.caducity.base.ui.icons.symbols.MenuBook
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.feature.tracking.CategoryEditorScreen as CategoryEditorScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.components.category.toComposeShape
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
import com.alorma.caducity.ui.components.threshold.thresholdLabel
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
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
  val bottomSheetState = rememberAppBottomSheetState()
  SideEffectHandler(viewModel, dialogState, snackbarState, bottomSheetState)

  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
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
        onAppearanceClick = viewModel::onAppearanceClick,
        onDurationClick = viewModel::onDurationClick,
        onDescriptionClick = viewModel::onDescriptionClick,
        onCanFreezeChange = viewModel::onCanFreezeChange,
        onDelete = viewModel::onDeleteClick,
      )
    }
  }
}

/** Everything fits in one screen: preview, name and a list of options that open sheets. */
@Composable
internal fun CategoryEditorContent(
  state: CategoryEditorState,
  onNameChange: (String) -> Unit,
  onAppearanceClick: () -> Unit,
  onDurationClick: () -> Unit,
  onDescriptionClick: () -> Unit,
  onCanFreezeChange: (Boolean) -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .imePadding()
        .padding(horizontal = 24.dp)
        .padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      AppearancePreview(state = state, onClick = onAppearanceClick)
      Text(
        text = stringResource(R.string.category_editor_appearance_hint),
        style = CaducityTheme.typography.bodySmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }

    OutlinedTextField(
      value = state.name,
      onValueChange = onNameChange,
      placeholder = { Text(stringResource(R.string.category_form_name)) },
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

    StyledSettingsGroup(
      title = {
        Text(
          text = stringResource(R.string.category_editor_options),
          style = CaducityTheme.typography.labelLarge,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      },
    ) {
      StyledSettingsCard(
        icon = { Icon(Symbols.Outlined.EventAvailable, contentDescription = null) },
        title = stringResource(R.string.category_editor_duration_row),
        subtitle = stringResource(R.string.category_editor_duration_row_help),
        subtitleMaxLines = 1,
        action = {
          OptionValue(
            value =
              if (state.usualDurationDays == null) {
                stringResource(R.string.category_editor_duration_none)
              } else {
                thresholdLabel(state.shownDurationDays)
              },
          )
        },
        onClick = onDurationClick,
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
      )
      StyledSettingsSwitchCard(
        icon = { Icon(Symbols.Outlined.AcUnit, contentDescription = null) },
        title = stringResource(R.string.category_editor_can_freeze),
        subtitle = stringResource(R.string.category_editor_can_freeze_help),
        state = state.canFreeze,
        onCheckedChange = onCanFreezeChange,
        shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
      )
      StyledSettingsCard(
        icon = { Icon(Symbols.Outlined.MenuBook, contentDescription = null) },
        title = stringResource(R.string.category_editor_description_row),
        subtitle = state.description.ifBlank { stringResource(R.string.category_editor_description_optional) },
        subtitleMaxLines = 1,
        action = {
          val actionRes =
            if (state.description.isBlank()) {
              R.string.category_editor_description_add
            } else {
              R.string.category_editor_description_edit
            }
          OptionValue(value = stringResource(actionRes))
        },
        onClick = onDescriptionClick,
        shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
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

/** Shape with its icon and a round ✏️ badge: the same pattern as a contact's photo. */
@Composable
private fun AppearancePreview(
  state: CategoryEditorState,
  onClick: () -> Unit,
) {
  Box(modifier = Modifier.size(128.dp)) {
    CategoryShapeIcon(
      shape = state.shape,
      icon = state.icon.imageVector,
      size = 128.dp,
      selected = true,
      modifier = Modifier.clip(state.shape.toComposeShape()).clickable(onClick = onClick),
    )
    Surface(
      onClick = onClick,
      shape = CircleShape,
      color = CaducityTheme.colorScheme.secondaryContainer,
      border = BorderStroke(3.dp, CaducityTheme.colorScheme.background),
      modifier = Modifier.align(Alignment.BottomEnd).size(40.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          Symbols.Outlined.Edit,
          contentDescription = stringResource(R.string.category_editor_appearance),
          tint = CaducityTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun OptionValue(value: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = value,
      style = CaducityTheme.typography.bodyLarge,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
    Icon(Symbols.Outlined.ChevronRight, contentDescription = null)
  }
}

@Composable
private fun SideEffectHandler(
  viewModel: CategoryEditorViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  bottomSheetState: AppBottomSheetState,
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
                  Text(pluralStringResource(R.plurals.category_delete_body, effect.productCount, effect.productCount))
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
        CategoryEditorSideEffect.ShowAppearanceSheet ->
          bottomSheetState.show {
            val current by viewModel.state.collectAsStateWithLifecycle()
            CategoryAppearanceSheet(
              name = current.name,
              shape = current.shape,
              icon = current.icon,
              onShapeChange = viewModel::onShapeChange,
              onIconChange = viewModel::onIconChange,
              onDone = { scope.launch { bottomSheetState.hide() } },
            )
          }
        // The Durada and Descripció sheets arrive in the next tasks.
        CategoryEditorSideEffect.ShowDurationSheet,
        CategoryEditorSideEffect.ShowDescriptionSheet,
        -> Unit
      }
    }
  }
}
