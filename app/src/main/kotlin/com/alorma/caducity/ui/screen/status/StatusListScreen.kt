package com.alorma.caducity.ui.screen.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.base.ui.icons.symbols.DeleteSweep
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductRowDetail
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.domain.model.toProductRow
import com.alorma.caducity.feature.tracking.StatusListScreen as StatusListScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.product.ProductRow
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun StatusListScreen(
  status: ExpiryStatus,
  onBack: () -> Unit,
  onOpenItemActions: (itemIds: List<String>) -> Unit,
  onOpenAdd: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: StatusListViewModel = koinViewModel { parametersOf(status) },
) {
  TrackScreen(screen = StatusListScreenEvent(status.name.lowercase()))
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = rememberAppDialogState()
  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        is StatusListNavigationSideEffect.OpenItemActions -> onOpenItemActions(effect.itemIds)
        StatusListNavigationSideEffect.OpenAdd -> onOpenAdd()
      }
    }
  }
  SideEffectHandler(viewModel, dialogState)

  val ready = state as? StatusListState.Ready
  val showDiscardAll = status == ExpiryStatus.EXPIRED && ready?.isEmpty == false
  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    topBar = {
      StatusTopBar(
        status = status,
        count = ready?.totalCount ?: 0,
        onBack = onBack,
        onDiscardAll = viewModel::onDiscardAllClick.takeIf { showDiscardAll },
      )
    },
  ) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      if (ready != null) {
        StatusListContent(ready = ready, viewModel = viewModel)
      }
    }
  }
}

/** Always folded: a standard M3E [TopAppBar] in the color of the status, with the count as subtitle. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusTopBar(
  status: ExpiryStatus,
  count: Int,
  onBack: () -> Unit,
  onDiscardAll: (() -> Unit)?,
) {
  val colors = status.colors()
  TopAppBar(
    title = { Text(stringResource(titleRes(status))) },
    subtitle = { Text(pluralStringResource(R.plurals.home_product_count, count, count)) },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(AppIcons.Back, contentDescription = stringResource(R.string.calendar_back))
      }
    },
    actions = {
      if (onDiscardAll != null) {
        IconButton(onClick = onDiscardAll) {
          Icon(Symbols.Outlined.DeleteSweep, contentDescription = stringResource(R.string.status_discard_all))
        }
      }
    },
    colors =
      TopAppBarDefaults.topAppBarColors(
        containerColor = colors.container,
        scrolledContainerColor = colors.container,
        navigationIconContentColor = colors.onContainer,
        titleContentColor = colors.onContainer,
        subtitleContentColor = colors.onContainer,
        actionIconContentColor = colors.onContainer,
      ),
  )
}

@Composable
private fun SideEffectHandler(
  viewModel: StatusListViewModel,
  dialogState: AppDialogState,
) {
  val cancelAction = stringResource(R.string.item_actions_cancel)
  val discardLabel = stringResource(R.string.status_discard_confirm)
  val discardBody = stringResource(R.string.status_discard_body)
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is StatusListSideEffect.ShowDiscardConfirm ->
          launch {
            val result =
              dialogState.showAlertDialog(
                title = {
                  Text(pluralStringResource(R.plurals.status_discard_title, effect.unitCount, effect.unitCount))
                },
                content = { Text(discardBody) },
                positiveButton = { Text(discardLabel) },
                negativeButton = { Text(cancelAction) },
                type = AppFeedbackType.Error,
              )
            if (result == DialogResult.Positive) viewModel.onDiscardConfirmed(effect.itemIds)
          }
      }
    }
  }
}

@Composable
private fun StatusListContent(
  ready: StatusListState.Ready,
  viewModel: StatusListViewModel,
  modifier: Modifier = Modifier,
) {
  val today = koinInject<TodayProvider>().today()
  Column(modifier = modifier.fillMaxSize()) {
    if (ready.isEmpty) {
      Box(modifier = Modifier.fillMaxSize()) {
        StatusEmpty(ready.status, onAdd = { viewModel.navigate(StatusListNavigation.AddFirst) })
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
      ) {
        ready.sections.forEach { section ->
          item(key = "header-${section.category.id}") {
            SectionHeader(section, Modifier.padding(top = 16.dp, bottom = 8.dp))
          }
          items(section.rows, key = { it.key }) { row ->
            val index = section.rows.indexOf(row)
            ProductRow(
              model = row.toRowModel(today),
              index = index,
              count = section.rows.size,
              onClick = { viewModel.navigate(StatusListNavigation.ItemActions(row.itemIds)) },
              onConsume = { viewModel.onConsume(row) },
              onFreeze = { viewModel.onFreeze(row) },
              onDiscard = { viewModel.onDiscard(row) },
            )
          }
        }
      }
    }
  }
}

private fun titleRes(status: ExpiryStatus): Int =
  when (status) {
    ExpiryStatus.EXPIRED -> R.string.status_title_expired
    ExpiryStatus.SOON -> R.string.status_title_soon
    ExpiryStatus.FRESH -> R.string.status_title_fresh
    ExpiryStatus.FROZEN -> R.string.status_title_frozen
  }

@Composable
private fun SectionHeader(
  section: StatusSection,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    CategoryShapeIcon(section.category.shape, CategoryIcons.vectorOf(section.category.icon), size = 36.dp)
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = section.category.name,
        style = CaducityTheme.typography.titleMedium,
        modifier = Modifier.weight(1f, fill = false).semantics { heading() },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = section.unitCount.toString(),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/** The row as the shared [ProductRow] shows it: no category icon (the list is already grouped by it). */
private fun StatusRow.toRowModel(today: LocalDate): ProductRowModel =
  entries.toProductRow(category = null, detail = ProductRowDetail.WithDays, today = today)

@Composable
private fun StatusEmpty(
  status: ExpiryStatus,
  onAdd: () -> Unit,
) {
  val (title, body) =
    when (status) {
      ExpiryStatus.EXPIRED -> R.string.status_empty_expired_title to R.string.status_empty_expired_body
      ExpiryStatus.SOON -> R.string.status_empty_soon_title to R.string.status_empty_soon_body
      ExpiryStatus.FRESH -> R.string.status_empty_fresh_title to R.string.status_empty_fresh_body
      ExpiryStatus.FROZEN -> R.string.status_empty_frozen_title to null
    }
  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(title),
      style = CaducityTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    if (body != null) {
      Text(
        text = stringResource(body),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
      )
    }
    if (status == ExpiryStatus.FRESH) {
      Button(onClick = onAdd, modifier = Modifier.padding(top = 20.dp).heightIn(min = 48.dp)) {
        Text(stringResource(R.string.status_empty_fresh_action))
      }
    }
  }
}
