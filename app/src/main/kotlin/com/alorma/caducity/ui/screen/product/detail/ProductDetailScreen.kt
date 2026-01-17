package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.ThermometerSnow
import com.alorma.caducity.base.ui.icons.Today
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.StatusBadge
import com.alorma.caducity.ui.components.StatusBadgeSize
import com.alorma.caducity.ui.components.calendar.CaducityWeekCalendar
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.feedback.AppFeedbackResource
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHostState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarHostState
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.product.detail.timeline.TimelineBulletAndLine
import com.alorma.caducity.ui.theme.CaducityTheme
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

@Composable
fun ProductDetailScreen(
  productId: String,
  onNavigateToAddInstance: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductDetailViewModel = koinViewModel { parametersOf(productId) }
) {
  val state = viewModel.state.collectAsStateWithLifecycle()

  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarHostState()

  SideEffectHandler(viewModel, snackbarState, dialogState)

  when (val currentState = state.value) {
    is ProductDetailState.Loading -> FullscreenLoading()

    is ProductDetailState.Success -> {
      ProductDetailSuccessContent(
        modifier = modifier,
        dialogState = dialogState,
        snackbarState = snackbarState,
        state = currentState,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onConsume = viewModel::onConsumeInstance,
        onFreeze = viewModel::onFreezeInstance,
        onDelete = viewModel::onDeleteInstance,
      )
    }

    is ProductDetailState.Error -> DetailError(currentState)
  }
}

@Composable
private fun ProductDetailSuccessContent(
  modifier: Modifier,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarHostState,
  state: ProductDetailState.Success,
  onNavigateToAddInstance: () -> Unit,
  onConsume: (ProductInstanceDetailUiModel) -> Unit,
  onFreeze: (ProductInstanceDetailUiModel) -> Unit,
  onDelete: (ProductInstanceDetailUiModel) -> Unit,
) {
  val userSelectedDate = remember {
    mutableStateOf(state.today)
  }

  val selectedScrollIndex = remember {
    derivedStateOf {
      findFirstIndex(
        date = userSelectedDate.value,
        datedContent = state.datedContent
      )
    }
  }

  val listState = rememberLazyListState(
    initialFirstVisibleItemIndex = selectedScrollIndex.value,
  )

  val weekCalendarState: WeekCalendarState = rememberWeekCalendarState(
    startDate = state.appCalendarConfig.startDate,
    endDate = state.appCalendarConfig.endDate,
    firstDayOfWeek = state.appCalendarConfig.firstDayOfWeek,
    firstVisibleWeekDate = state.appCalendarConfig.today,
  )

  val coroutineScope = rememberCoroutineScope()

  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    topBar = {
      StyledTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
        ),
        title = { Text(text = state.product.name) },
        navigationIcon = { NavigationIcon() },
        actions = {
          IconButton(
            onClick = {
              coroutineScope.launch {
                // Scroll list to today
                val todayIndex = findFirstIndex(
                  date = state.today,
                  datedContent = state.datedContent
                )
                listState.animateScrollToItem(todayIndex)

                // Scroll calendar to today
                weekCalendarState.animateScrollToDate(state.today)

                // Update user selected date
                userSelectedDate.value = state.today
              }
            }
          ) {
            Icon(
              imageVector = AppIcons.Today,
              contentDescription = "Scroll to today",
              tint = CaducityTheme.colorScheme.primary,
            )
          }
        },
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onNavigateToAddInstance,
      ) {
        Icon(
          imageVector = AppIcons.Add,
          contentDescription = null,
        )
      }
    },
  ) { paddingValues ->

    LaunchedEffect(selectedScrollIndex.value) {
      listState.animateScrollToItem(selectedScrollIndex.value)
    }

    LaunchedEffect(listState) {
      snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
        val date = state.datedContent.getOrNull(index)?.date ?: state.today

        weekCalendarState.animateScrollToDate(date)
      }
    }

    Column(
      modifier = Modifier.padding(paddingValues),
    ) {
      Surface(
        color = CaducityTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 2.dp,
      ) {
        CaducityWeekCalendar(
          modifier = Modifier.padding(bottom = 8.dp),
          todayColor = CaducityTheme.colorScheme.primary,
          weekCalendarState = weekCalendarState,
          appCalendarConfig = state.appCalendarConfig,
          contentPadding = PaddingValues(horizontal = 12.dp),
          onDateClick = { date -> userSelectedDate.value = date },
        )
      }

      LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 600.dp),
      ) {
        items(
          items = state.datedContent,
          key = { datedContent -> "product-${state.product.id}-dated-${datedContent.date}" },
          contentType = { "datedContent" },
        ) { datedContent ->
          DatedContent(
            state = datedContent,
            contentPadding = PaddingValues(
              start = 28.dp,
              end = 12.dp,
            ),
            onConsume = onConsume,
            onFreeze = onFreeze,
            onDelete = onDelete,
          )
        }
      }
    }
  }
}

private fun findFirstIndex(
  date: LocalDate,
  datedContent: ImmutableList<DateInstancesUiModel>,
): Int {
  if (datedContent.isEmpty()) return 0

  // Find exact match first
  val exactIndex = datedContent.indexOfFirst { it.date == date }
  if (exactIndex != -1) return exactIndex

  // Find the nearest date by calculating days difference using kotlinx-datetime
  val nearestIndex = datedContent
    .withIndex()
    .minByOrNull { (_, dated) ->
      abs(date.until(dated.date, DateTimeUnit.DAY))
    }
    ?.index

  return nearestIndex ?: 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatedContent(
  state: DateInstancesUiModel,
  onConsume: (ProductInstanceDetailUiModel) -> Unit,
  onFreeze: (ProductInstanceDetailUiModel) -> Unit,
  onDelete: (ProductInstanceDetailUiModel) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  val itemHeight = remember { mutableStateOf(24.dp) }
  val bulletOffset = remember { mutableStateOf(Offset.Zero) }
  val localDensity = LocalDensity.current

  val selectedInstance = remember { mutableStateOf<ProductInstanceDetailUiModel?>(null) }
  val sheetState = rememberModalBottomSheetState()

  Row(
    modifier = Modifier
      .padding(contentPadding)
      .onGloballyPositioned { coordinates ->
        with(localDensity) { itemHeight.value = coordinates.size.height.toDp() }
      }
      .fillMaxWidth()
      .then(modifier),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    TimelineBulletAndLine(
      itemHeight = itemHeight.value,
      bulletOffset = bulletOffset.value,
      onBulletPositionObtained = { bulletOffset.value = it },
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          vertical = 12.dp,
          horizontal = 16.dp
        ),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        StatusBadge(
          status = state.status,
          size = StatusBadgeSize.Large,
        )

        Text(
          text = "·",
          style = CaducityTheme.typography.labelMedium,
        )

        Text(
          text = state.text,
          style = CaducityTheme.typography.labelMedium,
        )
      }

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        maxItemsInEachRow = 3,
      ) {
        val statusColors = ExpirationDefaults.getSoftColors(state.status)

        val chipColors = SuggestionChipDefaults.suggestionChipColors(
          containerColor = statusColors.container,
        )

        state.instances.forEach { instance ->
          SuggestionChip(
            onClick = { selectedInstance.value = instance },
            colors = chipColors,
            label = { Text(text = instance.text) },
          )
        }
      }
    }
  }

  // Bottom sheet for instance actions
  selectedInstance.value?.let { instance ->
    ModalBottomSheet(
      onDismissRequest = { selectedInstance.value = null },
      sheetState = sheetState,
    ) {
      InstanceActionsBottomSheet(
        instance = instance,
        onConsume = {
          onConsume(instance)
          selectedInstance.value = null
        },
        onFreeze = {
          onFreeze(instance)
          selectedInstance.value = null
        },
        onDelete = {
          onDelete(instance)
          selectedInstance.value = null
        },
      )
    }
  }
}

@Composable
private fun InstanceActionsBottomSheet(
  instance: ProductInstanceDetailUiModel,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDelete: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 24.dp),
  ) {
    // Header with instance info
    Text(
      text = instance.text,
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
    )

    HorizontalDivider()

    // Consume action
    ListItem(
      headlineContent = { Text(stringResource(R.string.product_detail_action_consume)) },
      leadingContent = {
        Icon(
          imageVector = AppIcons.Cooking,
          contentDescription = null,
        )
      },
      modifier = Modifier.clickable(onClick = onConsume),
    )

    // Freeze action
    ListItem(
      headlineContent = { Text(stringResource(R.string.product_detail_action_freeze)) },
      leadingContent = {
        Icon(
          imageVector = AppIcons.ThermometerSnow,
          contentDescription = null,
        )
      },
      modifier = Modifier.clickable(onClick = onFreeze),
    )

    // Delete action
    ListItem(
      headlineContent = { Text(stringResource(R.string.product_detail_action_delete)) },
      leadingContent = {
        Icon(
          imageVector = AppIcons.Delete,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
        )
      },
      modifier = Modifier.clickable(onClick = onDelete),
    )
  }
}

@Composable
private fun DetailError(currentState: ProductDetailState.Error) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = currentState.message,
      style = MaterialTheme.typography.bodyLarge,
      color = CaducityTheme.colorScheme.error,
    )
  }
}

@Composable
private fun SideEffectHandler(
  viewModel: ProductDetailViewModel,
  snackbarState: AppSnackbarHostState,
  dialogState: AppDialogState
) {
  LaunchedEffect(viewModel.sideEffect) {
    viewModel.sideEffect.collect { effect ->
      when (effect) {
        ProductDetailSideEffect.InstanceConsumed -> {
          snackbarState.showSnackbar(
            message = R.string.success_instance_consumed,
            type = AppFeedbackType.Success,
          )
        }

        ProductDetailSideEffect.InstanceFrozen -> {
          snackbarState.showSnackbar(
            message = R.string.success_instance_frozen,
            type = AppFeedbackType.Status(InstanceStatus.Frozen),
          )
        }

        ProductDetailSideEffect.InstanceDeleted -> {
          snackbarState.showSnackbar(
            message = R.string.success_instance_deleted,
            type = AppFeedbackType.Success,
          )
        }

        ProductDetailSideEffect.ConsumeInstanceFailed -> {
          snackbarState.showSnackbar(
            message = R.string.error_consume_instance_failed,
            type = AppFeedbackType.Error,
          )
        }

        ProductDetailSideEffect.FreezeInstanceFailed -> {
          snackbarState.showSnackbar(
            message = R.string.error_freeze_instance_failed,
            type = AppFeedbackType.Error,
          )
        }

        ProductDetailSideEffect.DeleteInstanceFailed -> {
          snackbarState.showSnackbar(
            message = R.string.error_delete_instance_failed,
            type = AppFeedbackType.Error,
          )
        }

        is ProductDetailSideEffect.FreezeNotAvailable -> {
          snackbarState.showSnackbar(
            message = R.string.error_cannot_freeze_expired,
            type = AppFeedbackType.Status(effect.status),
          )
        }

        is ProductDetailSideEffect.ShowConsumeExpiredWarning -> {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_title
            ),
            text = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_message
            ),
            type = AppFeedbackType.Status(effect.instance.status),
            positiveButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_positive
            ),
            negativeButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_negative
            ),
          )
          if (result == DialogResult.Positive) {
            viewModel.onConsumeInstanceConfirmed(effect.instance)
          }
        }

        is ProductDetailSideEffect.ShowConsumeExpiredError -> {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.error_cannot_consume_expired
            ),
            text = AppFeedbackResource.AsResource(
              R.string.error_cannot_consume_expired_message
            ),
            type = AppFeedbackType.Status(effect.status),
            positiveButton = AppFeedbackResource.AsResource(
              R.string.error_cannot_consume_expired_positive
            ),
            negativeButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_negative
            ),
          )
          if (result == DialogResult.Positive) {
            viewModel.onDeleteInstance(effect.instance)
          }
        }
      }
    }
  }
}
