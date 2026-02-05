package com.alorma.caducity.ui.screen.category.detail

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.calendar.CaducityWeekCalendar
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
import com.alorma.caducity.ui.screen.category.detail.product.ProductTabContent
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CategoryDetailScreen(
  categoryId: String,
  onNavigateToAddInstance: (productId: String?) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CategoryDetailViewModel = koinViewModel { parametersOf(categoryId) }
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
    is CategoryDetailState.Loading -> FullscreenLoading()

    is CategoryDetailState.Success -> {
      CategoryDetailSuccessContent(
        modifier = modifier,
        bottomSheetState = bottomSheetState,
        dialogState = dialogState,
        snackbarState = snackbarState,
        state = currentState,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onItemClick = viewModel::onItemClick,
        onShowAddProductDialog = viewModel::onShowAddProductDialog,
        onDeleteCategoryClick = viewModel::onDeleteCategoryClick,
      )
    }

    is CategoryDetailState.Error -> DetailError(currentState)
  }
}

@Composable
private fun CategoryDetailSuccessContent(
  modifier: Modifier,
  bottomSheetState: AppBottomSheetState,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  state: CategoryDetailState.Success,
  onNavigateToAddInstance: (productId: String?) -> Unit,
  onItemClick: (ItemDetailUiModel) -> Unit,
  onShowAddProductDialog: () -> Unit,
  onDeleteCategoryClick: () -> Unit,
) {
  val pagerState = rememberPagerState(pageCount = { state.productTabs.size })
  val coroutineScope = rememberCoroutineScope()

  AppScaffold(
    modifier = modifier,
    bottomSheetState = bottomSheetState,
    dialogState = dialogState,
    snackbarState = snackbarState,
    topBar = {
      StyledTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
        ),
        title = { Text(text = state.category.name) },
        navigationIcon = { NavigationIcon() },
        actions = {
          IconButton(onClick = onDeleteCategoryClick) {
            Icon(
              imageVector = AppIcons.Delete,
              contentDescription = stringResource(R.string.category_detail_action_delete),
            )
          }
        },
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          // Get the currently selected product ID
          val selectedcategoryId = if (state.productTabs.isNotEmpty()) {
            val currentTab = state.productTabs[pagerState.currentPage]
            // Don't pass "other" as a product ID
            if (currentTab.id != "other") currentTab.id else null
          } else {
            null
          }
          onNavigateToAddInstance(selectedcategoryId)
        },
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

          if (state.productTabs.isNotEmpty()) {
            Row(
              modifier = Modifier.padding(
                top = 8.dp,
                end = 16.dp,
              ),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              SecondaryScrollableTabRow(
                modifier = Modifier.weight(1f),
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                divider = {},
                containerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
              ) {
                state.productTabs.forEachIndexed { index, productTab ->
                  Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                      coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                      }
                    },
                    text = { Text(text = productTab.name) },
                  )
                }
              }

              IconButton(
                modifier = Modifier,
                onClick = onShowAddProductDialog,
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
        val productTab = state.productTabs[page]
        ProductTabContent(
          productTab = productTab,
          onItemClick = onItemClick,
        )
      }
    }
  }
}

private fun AppBottomSheetState.ItemActionsBottomSheet(
  coroutineScope: CoroutineScope,
  item: ItemDetailUiModel,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDelete: () -> Unit,
) {
  coroutineScope.launch {
    show(
      appFeedbackType = AppFeedbackType.Status(item.status),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp),
      ) {
        // Header with item info
        Text(
          text = item.text,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )

        HorizontalDivider()

        // Consume action
        ListItem(
          headlineContent = { Text(stringResource(R.string.category_detail_action_consume)) },
          leadingContent = {
            Icon(
              imageVector = AppIcons.Cooking,
              contentDescription = null,
            )
          },
          modifier = Modifier.clickable {
            onConsume()
            coroutineScope.launch { this@ItemActionsBottomSheet.hide() }
          },
        )

        // Freeze action
        ListItem(
          headlineContent = { Text(stringResource(R.string.category_detail_action_freeze)) },
          leadingContent = {
            Icon(
              imageVector = AppIcons.ThermometerSnow,
              contentDescription = null,
            )
          },
          modifier = Modifier.clickable {
            onFreeze()
            coroutineScope.launch { this@ItemActionsBottomSheet.hide() }
          },
        )

        // Delete action
        ListItem(
          headlineContent = { Text(stringResource(R.string.category_detail_action_delete)) },
          leadingContent = {
            Icon(
              imageVector = AppIcons.Delete,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
            )
          },
          modifier = Modifier.clickable {
            onDelete()
            coroutineScope.launch { this@ItemActionsBottomSheet.hide() }
          },
        )
      }
    }
  }
}

@Composable
private fun DetailError(currentState: CategoryDetailState.Error) {
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
  viewModel: CategoryDetailViewModel,
  snackbarState: AppSnackbarState,
  dialogState: AppDialogState,
  bottomSheetState: AppBottomSheetState,
) {
  val backDispatcher = LocalOnBackPressedDispatcherOwner.current

  LaunchedEffect(viewModel.sideEffect) {
    viewModel.sideEffect.collect { effect ->
      when (effect) {
        CategoryDetailSideEffect.ItemConsumed -> launch {
          snackbarState.showSnackbar(
            message = R.string.success_item_consumed,
            type = AppFeedbackType.Success,
          )
        }

        CategoryDetailSideEffect.ItemFrozen -> launch {
          snackbarState.showSnackbar(
            message = R.string.success_item_frozen,
            type = AppFeedbackType.Status(ItemStatus.Frozen),
          )
        }

        CategoryDetailSideEffect.ItemDeleted -> launch {
          snackbarState.showSnackbar(
            message = R.string.success_item_deleted,
            type = AppFeedbackType.Success,
          )
        }

        CategoryDetailSideEffect.ConsumeItemFailed -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_consume_item_failed,
            type = AppFeedbackType.Error,
          )
        }

        CategoryDetailSideEffect.FreezeItemFailed -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_freeze_item_failed,
            type = AppFeedbackType.Error,
          )
        }

        CategoryDetailSideEffect.DeleteItemFailed -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_delete_item_failed,
            type = AppFeedbackType.Error,
          )
        }

        is CategoryDetailSideEffect.FreezeNotAvailable -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_cannot_freeze_expired,
            type = AppFeedbackType.Status(effect.status),
          )
        }

        is CategoryDetailSideEffect.ShowConsumeExpiredWarning -> launch {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_title
            ),
            text = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_message
            ),
            type = AppFeedbackType.Status(effect.item.status),
            positiveButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_positive
            ),
            negativeButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_negative
            ),
          )
          if (result == DialogResult.Positive) {
            viewModel.onConsumeItemConfirmed(effect.item)
          }
        }

        is CategoryDetailSideEffect.ShowConsumeExpiredError -> launch {
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
            viewModel.onDeleteItem(effect.item)
          }
        }

        CategoryDetailSideEffect.ShowAddProductDialog -> launch {
          var productName by mutableStateOf("")
          val result = dialogState.showAlertDialog(
            title = { Text(stringResource(R.string.category_detail_add_product_dialog_title)) },
            text = {
              OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text(stringResource(R.string.category_detail_add_product_dialog_label)) },
                placeholder = { Text(stringResource(R.string.category_detail_add_product_dialog_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
            },
            positiveButton = { Text(stringResource(R.string.category_detail_add_product_dialog_add)) },
            negativeButton = { Text(stringResource(R.string.category_detail_add_product_dialog_cancel)) },
            type = AppFeedbackType.Info,
          )
          if (result == DialogResult.Positive && productName.isNotBlank()) {
            viewModel.onCreateProduct(productName)
          }
        }

        is CategoryDetailSideEffect.ShowItemActionsBottomSheet -> launch {
          bottomSheetState.ItemActionsBottomSheet(
            coroutineScope = this@LaunchedEffect,
            item = effect.item,
            onConsume = {
              viewModel.onConsumeItem(effect.item)
              this.launch { }
            },
            onFreeze = {
              viewModel.onFreezeItem(effect.item)
              this.launch { }
            },
            onDelete = {
              viewModel.onDeleteItem(effect.item)
              this.launch { }
            },
          )
        }

        CategoryDetailSideEffect.ProductCreated -> launch {
          // Product created successfully - no feedback needed, it will appear in the list
        }

        CategoryDetailSideEffect.CreateProductFailed -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_create_product_failed,
            type = AppFeedbackType.Error,
          )
        }

        CategoryDetailSideEffect.ShowDeleteCategoryDialog -> launch {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.category_detail_delete_dialog_title
            ),
            text = AppFeedbackResource.AsResource(
              R.string.category_detail_delete_dialog_message
            ),
            type = AppFeedbackType.Error,
            positiveButton = AppFeedbackResource.AsResource(
              R.string.category_detail_delete_dialog_delete
            ),
            negativeButton = AppFeedbackResource.AsResource(
              R.string.category_detail_delete_dialog_cancel
            ),
          )
          if (result == DialogResult.Positive) {
            viewModel.onDeleteCategory()
          }
        }

        CategoryDetailSideEffect.CategoryDeleted -> launch {
          snackbarState.showSnackbar(
            message = R.string.success_category_deleted,
            type = AppFeedbackType.Success,
          )
          // Navigate back after successful deletion
          backDispatcher?.onBackPressedDispatcher?.onBackPressed()
        }

        CategoryDetailSideEffect.DeleteCategoryFailed -> launch {
          snackbarState.showSnackbar(
            message = R.string.error_delete_category_failed,
            type = AppFeedbackType.Error,
          )
        }
      }
    }
  }
}
