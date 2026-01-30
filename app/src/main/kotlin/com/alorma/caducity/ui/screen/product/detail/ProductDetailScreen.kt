package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.ThermometerSnow
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.StatusBadge
import com.alorma.caducity.ui.components.StatusBadgeSize
import com.alorma.caducity.ui.components.calendar.CaducityWeekCalendar
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.feedback.AppFeedbackResource
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.bottomsheet.rememberAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductDetailScreen(
  productId: String,
  onNavigateToAddInstance: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductDetailViewModel = koinViewModel { parametersOf(productId) }
) {
  val state = viewModel.state.collectAsStateWithLifecycle()

  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()
  val bottomSheetState = rememberAppBottomSheetState()

  SideEffectHandler(
    viewModel = viewModel,
    snackbarState = snackbarState,
    dialogState = dialogState,
    bottomSheetState = bottomSheetState,
  )

  when (val currentState = state.value) {
    is ProductDetailState.Loading -> FullscreenLoading()

    is ProductDetailState.Success -> {
      ProductDetailSuccessContent(
        modifier = modifier,
        dialogState = dialogState,
        snackbarState = snackbarState,
        state = currentState,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onInstanceClick = viewModel::onInstanceClick,
        onConsume = viewModel::onConsumeInstance,
        onFreeze = viewModel::onFreezeInstance,
        onDelete = viewModel::onDeleteInstance,
        onShowAddVariantDialog = viewModel::onShowAddVariantDialog,
      )
    }

    is ProductDetailState.Error -> DetailError(currentState)
  }
}

@Composable
private fun ProductDetailSuccessContent(
  modifier: Modifier,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  state: ProductDetailState.Success,
  onNavigateToAddInstance: () -> Unit,
  onInstanceClick: (ProductInstanceDetailUiModel) -> Unit,
  onConsume: (ProductInstanceDetailUiModel) -> Unit,
  onFreeze: (ProductInstanceDetailUiModel) -> Unit,
  onDelete: (ProductInstanceDetailUiModel) -> Unit,
  onShowAddVariantDialog: () -> Unit,
) {
  val pagerState = rememberPagerState(pageCount = { state.variantTabs.size })
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
    Column(
      modifier = Modifier.padding(paddingValues),
    ) {
      Surface(
        color = CaducityTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 2.dp,
      ) {
        Column {
          CaducityWeekCalendar(
            appCalendarConfig = state.appCalendarConfig,
            todayColor = CaducityTheme.colorScheme.surfaceContainerHighest,
            onDateClick = { },
          )

          if (state.variantTabs.isNotEmpty()) {
            Row(
              modifier = Modifier.padding(end = 16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              SecondaryScrollableTabRow(
                modifier = Modifier.weight(1f),
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                divider = {},
                containerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
              ) {
                state.variantTabs.forEachIndexed { index, variantTab ->
                  Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                      coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                      }
                    },
                    text = { Text(text = variantTab.name) },
                  )
                }
              }

              IconButton(
                modifier = Modifier,
                onClick = onShowAddVariantDialog,
              ) {
                Icon(
                  modifier = Modifier.size(18.dp),
                  imageVector = AppIcons.Add,
                  contentDescription = null,
                )
              }
            }
          }
        }
      }

      // Horizontal Pager for variant content
      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
      ) { page ->
        val variantTab = state.variantTabs[page]
        VariantTabContent(
          variantTab = variantTab,
          onInstanceClick = onInstanceClick,
        )
      }
    }
  }
}

@Composable
private fun VariantTabContent(
  variantTab: ProductDetailVariantTabUiModel,
  onInstanceClick: (ProductInstanceDetailUiModel) -> Unit,
) {
  when (variantTab) {
    is ProductDetailVariantTabUiModel.Empty -> {
      // Show empty state for variants with no instances
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(R.string.product_detail_variant_empty_state),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    is ProductDetailVariantTabUiModel.WithInstances -> {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Show each status group
        variantTab.datedInstancesGroups.forEach { datedInstances ->
          StatusGroupCard(
            datedInstances = datedInstances,
            onInstanceClick = onInstanceClick,
          )
        }
      }
    }
  }
}

@Composable
private fun StatusGroupCard(
  datedInstances: DateInstancesUiModel,
  onInstanceClick: (ProductInstanceDetailUiModel) -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Show expiration date and status
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      StatusBadge(
        status = datedInstances.status,
        size = StatusBadgeSize.Large,
      )

      if (datedInstances.text.isNotEmpty()) {
        Text(
          text = "·",
          style = CaducityTheme.typography.labelMedium,
        )

        Text(
          text = datedInstances.text,
          style = CaducityTheme.typography.labelMedium,
        )
      }
    }

    // Show instances
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      maxItemsInEachRow = 3,
    ) {
      val statusColors = ExpirationDefaults.getSoftColors(datedInstances.status)

      val chipColors = SuggestionChipDefaults.suggestionChipColors(
        containerColor = statusColors.container,
      )

      datedInstances.instances.forEach { instance ->
        SuggestionChip(
          onClick = { onInstanceClick(instance) },
          colors = chipColors,
          label = { Text(text = instance.text) },
        )
      }
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
  snackbarState: AppSnackbarState,
  dialogState: AppDialogState,
  bottomSheetState: AppBottomSheetState,
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

        ProductDetailSideEffect.ShowAddVariantDialog -> {
          var variantName by mutableStateOf("")
          val result = dialogState.showAlertDialog(
            title = { Text(stringResource(R.string.product_detail_add_variant_dialog_title)) },
            text = {
              OutlinedTextField(
                value = variantName,
                onValueChange = { variantName = it },
                label = { Text(stringResource(R.string.product_detail_add_variant_dialog_label)) },
                placeholder = { Text(stringResource(R.string.product_detail_add_variant_dialog_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
            },
            positiveButton = { Text(stringResource(R.string.product_detail_add_variant_dialog_add)) },
            negativeButton = { Text(stringResource(R.string.product_detail_add_variant_dialog_cancel)) },
            type = AppFeedbackType.Info,
          )
          if (result == DialogResult.Positive && variantName.isNotBlank()) {
            viewModel.onCreateVariant(variantName)
          }
        }

        is ProductDetailSideEffect.ShowInstanceActionsBottomSheet -> launch {
          bottomSheetState.show(
            onDismissRequest = {},
          ) {
            InstanceActionsBottomSheet(
              instance = effect.instance,
              onConsume = { viewModel.onConsumeInstance(effect.instance) },
              onFreeze = { viewModel.onFreezeInstance(effect.instance) },
              onDelete = { viewModel.onDeleteInstance(effect.instance) },
            )
          }
        }

        ProductDetailSideEffect.VariantCreated -> {
          // Variant created successfully - no feedback needed, it will appear in the list
        }

        ProductDetailSideEffect.CreateVariantFailed -> {
          snackbarState.showSnackbar(
            message = R.string.error_create_variant_failed,
            type = AppFeedbackType.Error,
          )
        }
      }
    }
  }
}
