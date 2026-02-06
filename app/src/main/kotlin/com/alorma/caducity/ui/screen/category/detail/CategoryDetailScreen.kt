package com.alorma.caducity.ui.screen.category.detail

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.ArrowDown
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.filled.Broom
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.ProductDeletionStrategy
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
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.category.detail.product.ProductTabContent
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults
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
    onCreateProduct = viewModel::onCreateProduct,
    onDeleteCategory = viewModel::onDeleteCategory,
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
        onShowAddProductDialog = viewModel::onShowAddProductDialog,
        onDeleteProductClick = viewModel::onDeleteProductClick,
        onClearProductItemsClick = viewModel::onClearProductItemsClick,
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
  onShowAddProductDialog: () -> Unit,
  onDeleteProductClick: (productId: String) -> Unit,
  onClearProductItemsClick: (productId: String) -> Unit,
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
      val currentTab = state.productTabs.getOrNull(pagerState.currentPage)

      val checked = remember { mutableStateOf(false) }
      val size = SplitButtonDefaults.MediumContainerHeight

      val buttonColors = ButtonDefaults.filledTonalButtonColors()

      SplitButtonLayout(
        modifier = Modifier.heightIn(size),
        leadingButton = {
          SplitButtonDefaults.LeadingButton(
            colors = buttonColors,
            shapes = SplitButtonDefaults.leadingButtonShapesFor(size),
            contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(size),
            onClick = {
              val selectedCategoryId = if (state.productTabs.isNotEmpty()) {
                currentTab?.id
              } else {
                null
              }
              onNavigateToAddInstance(selectedCategoryId)
            },
          ) {
            Text(
              text = stringResource(R.string.category_detail_add_item_button),
              style = ButtonDefaults.textStyleFor(size)
            )
          }
        },
        trailingButton = {
          Box {
            val rotation: Float by animateFloatAsState(
              targetValue = if (checked.value) {
                180f
              } else
                0f,
              label = "Trailing Icon Rotation",
            )

            TooltipBox(
              positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above,
              ),
              tooltip = { PlainTooltip { Text(stringResource(R.string.category_detail_product_actions_tooltip)) } },
              state = rememberTooltipState(),
            ) {
              SplitButtonDefaults.TrailingButton(
                modifier = Modifier.heightIn(size),
                colors = buttonColors,
                checked = checked.value,
                shapes = SplitButtonDefaults.trailingButtonShapesFor(size),
                contentPadding = SplitButtonDefaults.trailingButtonContentPaddingFor(size),
                onCheckedChange = { checked.value = it },
              ) {
                Icon(
                  modifier = Modifier
                    .size(SplitButtonDefaults.TrailingIconSize)
                    .graphicsLayer { this.rotationZ = rotation },
                  imageVector = AppIcons.ArrowDown,
                  contentDescription = stringResource(R.string.product_delete_menu),
                )
              }
            }

            DropdownMenu(
              expanded = checked.value,
              onDismissRequest = { checked.value = false }
            ) {
              // Show clear items option only if current tab is a product (not "Other")
              if (currentTab?.id != null) {
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.product_clear_items_action)) },
                  onClick = {
                    checked.value = false
                    onClearProductItemsClick(currentTab.id)
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = AppIcons.Outlined.Broom,
                      contentDescription = null,
                    )
                  }
                )

                DropdownMenuItem(
                  text = { Text(stringResource(R.string.product_delete_action)) },
                  onClick = {
                    checked.value = false
                    onDeleteProductClick(currentTab.id)
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = AppIcons.Delete,
                      contentDescription = null,
                      tint = CaducityTheme.colorScheme.error
                    )
                  }
                )
              }
            }
          }
        },
      )
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
  onCreateProduct: (String) -> Unit,
  onDeleteCategory: () -> Unit,
) {
  val backDispatcher = LocalOnBackPressedDispatcherOwner.current

  LaunchedEffect(viewModel.sideEffect) {
    viewModel.sideEffect.collect { effect ->
      when (effect) {
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
            onCreateProduct(productName)
          }
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

        is CategoryDetailSideEffect.ShowClearProductItemsDialog -> launch {
          bottomSheetState.showClearItemsBottomSheet(
            this,
            onClearConsumed = {
              viewModel.onClearProductItems(effect.productId, clearAll = false)
            },
            onClearAll = {
              viewModel.onClearProductItems(effect.productId, clearAll = true)
            },
          )
        }

        is CategoryDetailSideEffect.ShowDeleteProductDialog -> launch {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.product_delete_dialog_title
            ),
            text = AppFeedbackResource.AsResource(
              R.string.product_delete_dialog_message
            ),
            type = AppFeedbackType.Error,
            positiveButton = AppFeedbackResource.AsResource(
              R.string.product_delete_dialog_delete
            ),
            negativeButton = AppFeedbackResource.AsResource(
              R.string.product_delete_dialog_cancel
            ),
          )
          if (result == DialogResult.Positive) {
            effect.onDeleteProduct(
              effect.productId,
              ProductDeletionStrategy.CascadeDelete,
            )
          }
        }

        is CategoryDetailSideEffect.ShowDeleteProductWithItemsDialog -> launch {
          bottomSheetState.showDeleteProductWithItemsBottomSheet(
            this,
            effect.activeItemCount,
            effect.availableProducts,
            onMoveToStandalone = {
              effect.onDeleteProduct(
                effect.productId,
                ProductDeletionStrategy.MoveToStandalone,
              )
            },
            onMoveToProduct = { targetProductId ->
              effect.onDeleteProduct(
                effect.productId,
                ProductDeletionStrategy.MoveToProduct(targetProductId),
              )
            },
            onCascadeDelete = {
              effect.onDeleteProduct(
                effect.productId,
                ProductDeletionStrategy.CascadeDelete,
              )
            },
          )
        }

        CategoryDetailSideEffect.ProductDeleted -> launch {
          bottomSheetState.hide()
          snackbarState.showSnackbar(
            message = R.string.success_product_deleted,
            type = AppFeedbackType.Success,
          )
        }

        CategoryDetailSideEffect.DeleteProductFailed -> launch {
          bottomSheetState.hide()
          snackbarState.showSnackbar(
            message = R.string.error_delete_product_failed,
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
            onDeleteCategory()
          }
        }

        CategoryDetailSideEffect.CategoryDeleted -> launch {
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

private fun AppBottomSheetState.showDeleteProductWithItemsBottomSheet(
  coroutineScope: CoroutineScope,
  itemCount: Int,
  availableProducts: List<CategoryProductTabUiModel>,
  onMoveToStandalone: () -> Unit,
  onMoveToProduct: (String) -> Unit,
  onCascadeDelete: () -> Unit,
) {
  var showProductSelection by mutableStateOf(false)

  coroutineScope.launch {
    show {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        if (!showProductSelection) {
          // Main options screen
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Text(
              text = stringResource(R.string.product_delete_with_items_title, itemCount),
              style = MaterialTheme.typography.titleLarge,
            )

            Text(
              text = stringResource(R.string.product_delete_with_items_message),
              style = MaterialTheme.typography.bodyMedium,
            )

            // Options group
            StyledSettingsGroup {
              // Determine shape positions based on available products
              val hasProductOption = availableProducts.isNotEmpty()
              val standalonePosition =
                if (hasProductOption) ShapePosition.Start else ShapePosition.Start
              val productPosition = ShapePosition.Middle
              val deletePosition = if (hasProductOption) ShapePosition.End else ShapePosition.End

              // Option 1: Move to standalone items
              StyledSettingsCard(
                title = stringResource(R.string.product_delete_option_move_to_standalone),
                subtitle = stringResource(R.string.product_delete_option_move_to_standalone_desc),
                onClick = onMoveToStandalone,
                position = standalonePosition,
              )

              // Option 2: Move to another product (only if there are other products)
              if (hasProductOption) {
                StyledSettingsCard(
                  title = stringResource(R.string.product_delete_option_move_to_product),
                  subtitle = stringResource(R.string.product_delete_option_move_to_product_desc),
                  onClick = { showProductSelection = true },
                  position = productPosition,
                )
              }

              // Option 3: Delete all items (cascade delete)
              StyledSettingsCard(
                action = {
                  Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = null,
                  )
                },
                title = stringResource(R.string.product_delete_option_cascade_delete),
                subtitle = stringResource(R.string.product_delete_option_cascade_delete_desc),
                colors = SettingsTileDefaults.colors(
                  containerColor = CaducityTheme.colorScheme.errorContainer,
                  titleColor = CaducityTheme.colorScheme.onErrorContainer,
                  subtitleColor = CaducityTheme.colorScheme.onErrorContainer,
                  iconColor = CaducityTheme.colorScheme.onErrorContainer,
                  actionColor = CaducityTheme.colorScheme.onErrorContainer,
                ),
                onClick = onCascadeDelete,
                position = deletePosition,
              )
            }
          }
        } else {
          // Product selection screen with top bar
          StyledTopAppBar(
            title = { Text(stringResource(R.string.product_delete_select_target_title)) },
            navigationIcon = {
              IconButton(onClick = { showProductSelection = false }) {
                Icon(
                  imageVector = AppIcons.Back,
                  contentDescription = stringResource(R.string.product_delete_select_target_back),
                )
              }
            }
          )

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Text(
              text = stringResource(R.string.product_delete_select_target_message, itemCount),
              style = MaterialTheme.typography.bodyMedium,
            )

            // List all available products with StyledSettingsGroup
            StyledSettingsGroup {
              availableProducts.forEachIndexed { index, product ->
                val position = when {
                  availableProducts.size == 1 -> ShapePosition.Single
                  index == 0 -> ShapePosition.Start
                  index == availableProducts.size - 1 -> ShapePosition.End
                  else -> ShapePosition.Middle
                }

                StyledSettingsCard(
                  title = product.name,
                  onClick = {
                    product.id?.let { onMoveToProduct(it) }
                  },
                  position = position,
                )
              }
            }
          }
        }
      }
    }
  }
}


private fun AppBottomSheetState.showClearItemsBottomSheet(
  coroutineScope: CoroutineScope,
  onClearConsumed: () -> Unit,
  onClearAll: () -> Unit,
) {
  coroutineScope.launch {
    show {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = stringResource(R.string.product_clear_items_dialog_title),
          style = MaterialTheme.typography.titleLarge,
        )

        Text(
          text = stringResource(R.string.product_clear_items_dialog_message),
          style = MaterialTheme.typography.bodyMedium,
        )

        // Options group
        StyledSettingsGroup {
          // Option 1: Clear consumed items only
          StyledSettingsCard(
            icon = {
              Icon(
                imageVector = AppIcons.Outlined.Broom,
                contentDescription = null,
              )
            },
            title = stringResource(R.string.product_clear_consumed_only),
            subtitle = stringResource(R.string.product_clear_consumed_only_desc),
            onClick = onClearConsumed,
            position = ShapePosition.Start,
          )

          // Option 2: Clear all items
          StyledSettingsCard(
            icon = {
              Icon(
                imageVector = AppIcons.Delete,
                contentDescription = null,
                tint = CaducityTheme.colorScheme.error,
              )
            },
            title = stringResource(R.string.product_clear_all_items),
            subtitle = stringResource(R.string.product_clear_all_items_desc),
            onClick = onClearAll,
            position = ShapePosition.End,
          )
        }
      }
    }
  }
}
