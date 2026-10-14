package com.alorma.caducity.ui.screen.settings.durations

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Lightbulb
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.Undo
import com.alorma.caducity.feature.tracking.CategoryDurationsScreen as CategoryDurationsScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.connectedShape
import com.alorma.caducity.ui.components.threshold.ThresholdSlider
import com.alorma.caducity.ui.components.threshold.thresholdLabel
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryDurationsScreen(
  modifier: Modifier = Modifier,
  viewModel: CategoryDurationsViewModel = koinViewModel(),
) {
  TrackScreen(screen = CategoryDurationsScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()

  val restoreTitle = stringResource(R.string.durations_restore_title)
  val restoreBody = stringResource(R.string.durations_restore_body)
  val restoreConfirm = stringResource(R.string.durations_restore_confirm)
  val restoreCancel = stringResource(R.string.durations_restore_cancel)
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        CategoryDurationsSideEffect.ShowRestoreConfirm ->
          launch {
            val result =
              dialogState.showAlertDialog(
                title = { Text(restoreTitle) },
                content = { Text(restoreBody) },
                positiveButton = { Text(restoreConfirm) },
                negativeButton = { Text(restoreCancel) },
                type = AppFeedbackType.Info,
              )
            if (result == DialogResult.Positive) viewModel.onRestoreConfirmed()
          }
        CategoryDurationsSideEffect.Restored ->
          launch { snackbarState.showSnackbar(message = R.string.durations_restored, type = AppFeedbackType.Success) }
        CategoryDurationsSideEffect.Failed ->
          launch { snackbarState.showSnackbar(message = R.string.durations_failed, type = AppFeedbackType.Error) }
      }
    }
  }

  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = { Text(stringResource(R.string.durations_title)) },
      )
    },
  ) { padding ->
    ResponsiveSettingsContainer(modifier = Modifier.padding(padding)) {
      when (val current = state) {
        CategoryDurationsState.Loading -> Unit
        is CategoryDurationsState.Ready ->
          DurationsList(
            state = current,
            onRowClick = viewModel::onRowClick,
            onDaysChange = viewModel::onDaysChange,
            onRestore = viewModel::onRestoreClick,
          )
      }
    }
  }
}

@Composable
private fun DurationsList(
  state: CategoryDurationsState.Ready,
  onRowClick: (String) -> Unit,
  onDaysChange: (String, Int) -> Unit,
  onRestore: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
  ) {
    item {
      Text(
        text = stringResource(R.string.durations_help),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).padding(bottom = 8.dp),
      )
    }
    itemsIndexed(state.rows, key = { _, row -> row.category.id }) { index, row ->
      DurationRow(
        row = row,
        index = index,
        count = state.rows.size,
        expanded = state.expandedId == row.category.id,
        onClick = { onRowClick(row.category.id) },
        onDaysChange = { onDaysChange(row.category.id, it) },
      )
    }
    item {
      TextButton(onClick = onRestore, modifier = Modifier.padding(top = 12.dp)) {
        Icon(Symbols.Outlined.Undo, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        Text(stringResource(R.string.durations_restore))
      }
    }
  }
}

/** Slider position while a category has no usual duration. */
private const val SLIDER_START_DAYS = 7

@Composable
private fun DurationRow(
  row: CategoryDurationRow,
  index: Int,
  count: Int,
  expanded: Boolean,
  onClick: () -> Unit,
  onDaysChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = CaducityTheme.colorScheme
  val label = row.usualDays?.let { thresholdLabel(it) }
  Card(
    onClick = onClick,
    shape = connectedShape(index, count),
    colors =
      CardDefaults.cardColors(
        containerColor = if (expanded) colors.surfaceContainerHigh else colors.surfaceContainer,
      ),
    modifier = modifier.fillMaxWidth().animateContentSize(),
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CategoryShapeIcon(
          shape = row.category.shape,
          icon = row.category.icon.imageVector,
          size = 40.dp,
          selected = expanded,
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = row.category.name,
            style = CaducityTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 1,
          )
          Text(
            text =
              if (label == null) {
                stringResource(R.string.durations_row_default)
              } else {
                stringResource(R.string.durations_row_notify, label)
              },
            style = CaducityTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
          )
        }
        if (label != null) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (expanded) colors.primary else colors.surface,
            contentColor = if (expanded) colors.onPrimary else colors.onSurface,
          ) {
            Text(
              text = label,
              style = CaducityTheme.typography.labelLarge,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            )
          }
        }
      }
      if (expanded) {
        ThresholdSlider(
          days = row.usualDays ?: SLIDER_START_DAYS,
          onDaysChange = onDaysChange,
          modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
        val suggested = row.suggestedDays
        if (suggested != null && suggested != row.usualDays) {
          Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              Symbols.Outlined.Lightbulb,
              contentDescription = null,
              tint = colors.primary,
              modifier = Modifier.size(20.dp),
            )
            Text(
              text = stringResource(R.string.durations_suggested_hint, row.category.name, thresholdLabel(suggested)),
              style = CaducityTheme.typography.bodyMedium,
              color = colors.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}
