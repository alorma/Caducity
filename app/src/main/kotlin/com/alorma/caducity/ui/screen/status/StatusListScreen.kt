package com.alorma.caducity.ui.screen.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
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
import com.alorma.caducity.ui.components.feedback.snackbar.snackbarAnchor
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

  StatusListLayout(
    status = status,
    ready = state as? StatusListState.Ready,
    today = koinInject<TodayProvider>().today(),
    dialogState = dialogState,
    onBack = onBack,
    onRowClick = { row -> viewModel.navigate(StatusListNavigation.ItemActions(row.itemIds)) },
    onConsume = viewModel::onConsume,
    onFreeze = viewModel::onFreeze,
    onDiscard = viewModel::onDiscard,
    onAdd = { viewModel.navigate(StatusListNavigation.AddFirst) },
    onDiscardAll = viewModel::onDiscardAllClick,
    modifier = modifier,
  )
}

@Composable
internal fun StatusListLayout(
  status: ExpiryStatus,
  ready: StatusListState.Ready?,
  today: LocalDate,
  dialogState: AppDialogState,
  onBack: () -> Unit,
  onRowClick: (StatusRow) -> Unit,
  onConsume: (StatusRow) -> Unit,
  onFreeze: (StatusRow) -> Unit,
  onDiscard: (StatusRow) -> Unit,
  onAdd: () -> Unit,
  onDiscardAll: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val showDiscardAll = status == ExpiryStatus.EXPIRED && ready?.isEmpty == false
  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    topBar = {
      StatusTopBar(
        status = status,
        count = ready?.totalCount ?: 0,
        soonDays = ready?.soonDays,
        onBack = onBack,
      )
    },
    bottomBar = {
      if (showDiscardAll) DiscardAllBar(onDiscardAll = onDiscardAll)
    },
  ) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      if (ready != null) {
        StatusListContent(
          ready = ready,
          today = today,
          onRowClick = onRowClick,
          onConsume = onConsume,
          onFreeze = onFreeze,
          onDiscard = onDiscard,
          onAdd = onAdd,
        )
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
  soonDays: Int?,
  onBack: () -> Unit,
) {
  val colors = status.colors()
  val countText = pluralStringResource(R.plurals.home_product_count, count, count)
  val subtitle =
    if (status == ExpiryStatus.SOON && soonDays != null) {
      stringResource(
        R.string.status_soon_subtitle,
        countText,
        pluralStringResource(R.plurals.status_soon_window, soonDays, soonDays),
      )
    } else {
      countText
    }
  TopAppBar(
    modifier = Modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
    title = { Text(stringResource(titleRes(status))) },
    subtitle = { Text(subtitle) },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(AppIcons.Back, contentDescription = stringResource(R.string.calendar_back))
      }
    },
    colors =
      TopAppBarDefaults.topAppBarColors(
        containerColor = colors.container,
        scrolledContainerColor = colors.container,
        navigationIconContentColor = colors.onContainer,
        titleContentColor = colors.onContainer,
        subtitleContentColor = colors.onContainer,
      ),
  )
}

/** Caducats: the warning and a full-width red 56 dp button pinned at the bottom. */
@Composable
private fun DiscardAllBar(onDiscardAll: () -> Unit) {
  val expired = CaducityTheme.colors.expired
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .snackbarAnchor()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = stringResource(R.string.status_expired_warning),
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
    Button(
      onClick = onDiscardAll,
      modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
      colors = ButtonDefaults.buttonColors(containerColor = expired.strong, contentColor = expired.container),
    ) {
      Icon(Symbols.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(8.dp))
      Text(stringResource(R.string.status_discard_all))
    }
  }
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
                  Text(pluralStringResource(R.plurals.status_discard_title, effect.productCount, effect.productCount))
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
  today: LocalDate,
  onRowClick: (StatusRow) -> Unit,
  onConsume: (StatusRow) -> Unit,
  onFreeze: (StatusRow) -> Unit,
  onDiscard: (StatusRow) -> Unit,
  onAdd: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    if (ready.isEmpty) {
      Box(modifier = Modifier.fillMaxSize()) {
        StatusEmpty(ready.status, onAdd = onAdd)
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
              onClick = { onRowClick(row) },
              onConsume = { onConsume(row) },
              onFreeze = { onFreeze(row) },
              onDiscard = { onDiscard(row) },
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
        text = section.productCount.toString(),
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
