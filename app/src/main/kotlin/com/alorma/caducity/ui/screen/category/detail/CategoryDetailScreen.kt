package com.alorma.caducity.ui.screen.category.detail

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.remember
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
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.MoreVert
import com.alorma.caducity.ui.adaptive.rememberIsExpanded
import com.alorma.caducity.ui.components.calendar.AppCalendarConfig
import com.alorma.caducity.ui.components.calendar.CaducityMonthCalendar
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
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CategoryDetailScreen(
  categoryId: String,
  onNavigateToAddInstance: (productId: String?) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CategoryDetailViewModel = koinViewModel { parametersOf(categoryId) },
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

    is CategoryDetailState.Empty -> {
      CategoryDetailEmptyContent(
        modifier = modifier,
        dialogState = dialogState,
        snackbarState = snackbarState,
        state = currentState,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onShowAddProductDialog = viewModel::onShowAddProductDialog,
        onDeleteCategoryClick = viewModel::onDeleteCategoryClick,
      )
    }

    is CategoryDetailState.Success -> {
      CategoryDetailSuccessContent(
        modifier = modifier,
        bottomSheetState = bottomSheetState,
        dialogState = dialogState,
        snackbarState = snackbarState,
        state = currentState,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onShowAddProductDialog = viewModel::onShowAddProductDialog,
        onDeleteCategoryClick = viewModel::onDeleteCategoryClick,
      )
    }

    is CategoryDetailState.Error -> DetailError(currentState)
  }
}

@Composable
private fun CategoryDetailEmptyContent(
  modifier: Modifier,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  state: CategoryDetailState.Empty,
  onNavigateToAddInstance: (productId: String?) -> Unit,
  onShowAddProductDialog: () -> Unit,
  onDeleteCategoryClick: () -> Unit,
) {
  val isExpanded = rememberIsExpanded()

  AppScaffold(
    modifier = modifier,
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
          CategoryDetailOverflowMenu(onDeleteCategoryClick = onDeleteCategoryClick)
        },
      )
    },
  ) { paddingValues ->
    if (isExpanded) {
      CategoryDetailEmptyExpandedLayout(
        modifier = Modifier,
        paddingValues = paddingValues,
        state = state,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onShowAddProductDialog = onShowAddProductDialog,
      )
    } else {
      CategoryDetailEmptyCompactLayout(
        modifier = Modifier.padding(paddingValues),
        state = state,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onShowAddProductDialog = onShowAddProductDialog,
      )
    }
  }
}

@Composable
private fun CategoryDetailEmptyCompactLayout(
  state: CategoryDetailState.Empty,
  onNavigateToAddInstance: (productId: String?) -> Unit,
  onShowAddProductDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    Surface(
      color = CaducityTheme.colorScheme.surfaceContainerHigh,
      shadowElevation = 2.dp,
    ) {
      CaducityWeekCalendar(
        appCalendarConfig = state.appCalendarConfig,
        todayColor = CaducityTheme.colorScheme.surfaceContainerHighest,
        onDateClick = { },
      )
    }

    // Empty state content
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text(
          text = stringResource(R.string.category_detail_empty_category_title),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = stringResource(R.string.category_detail_empty_category_message),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp),
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
          OutlinedButton(
            onClick = onShowAddProductDialog,
          ) {
            Text(stringResource(R.string.category_detail_empty_category_create_product))
          }
          Button(
            onClick = { onNavigateToAddInstance(null) },
          ) {
            Text(stringResource(R.string.category_detail_empty_category_add_item))
          }
        }
      }
    }
  }
}

/**
 * Overflow menu for category detail actions.
 * Currently contains delete action, can be extended with more actions in the future.
 */
@Composable
private fun CategoryDetailOverflowMenu(
  onDeleteCategoryClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  IconButton(onClick = { expanded = true }) {
    Icon(
      imageVector = AppIcons.MoreVert,
      contentDescription = stringResource(R.string.category_detail_action_menu),
    )
  }

  DropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = false },
  ) {
    DropdownMenuItem(
      text = { Text(stringResource(R.string.category_detail_action_delete)) },
      onClick = {
        expanded = false
        onDeleteCategoryClick()
      },
      leadingIcon = {
        Icon(
          imageVector = AppIcons.Delete,
          contentDescription = null,
        )
      },
    )
  }
}

/**
 * Reusable calendar pane for tablet expanded layout.
 * Shows a month calendar in a surface with consistent styling.
 */
@Composable
private fun CategoryDetailCalendarPane(
  appCalendarConfig: AppCalendarConfig,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = Modifier
      .fillMaxHeight()
      .then(modifier),
    color = CaducityTheme.colorScheme.surfaceContainerHigh,
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      CaducityMonthCalendar(
        appCalendarConfig = appCalendarConfig,
        onDateClick = { },
      )
    }
  }
}

@Composable
private fun CategoryDetailEmptyExpandedLayout(
  state: CategoryDetailState.Empty,
  paddingValues: PaddingValues,
  onNavigateToAddInstance: (productId: String?) -> Unit,
  onShowAddProductDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxSize()
      .padding(top = paddingValues.calculateTopPadding())
  ) {
    // Left pane: Month calendar (1/3)
    CategoryDetailCalendarPane(
      modifier = Modifier.weight(0.33f),
      appCalendarConfig = state.appCalendarConfig,
    )

    // Right pane: Empty message (2/3)
    Box(
      modifier = Modifier
        .weight(0.67f)
        .fillMaxHeight(),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text(
          text = stringResource(R.string.category_detail_empty_category_title),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = stringResource(R.string.category_detail_empty_category_message),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp),
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
          OutlinedButton(
            onClick = onShowAddProductDialog,
          ) {
            Text(stringResource(R.string.category_detail_empty_category_create_product))
          }
          Button(
            onClick = { onNavigateToAddInstance(null) },
          ) {
            Text(stringResource(R.string.category_detail_empty_category_add_item))
          }
        }
      }
    }
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
  onDeleteCategoryClick: () -> Unit,
) {
  val isExpanded = rememberIsExpanded()

  // Hoist PagerState to preserve across layout changes
  val pagerState = rememberPagerState(
    initialPage = 0,
    pageCount = { state.productTabs.size.coerceAtLeast(1) }
  )

  // Handle case where current page is out of bounds after tabs change
  LaunchedEffect(state.productTabs.size) {
    if (state.productTabs.isNotEmpty() && pagerState.currentPage >= state.productTabs.size) {
      pagerState.scrollToPage(0)
    }
  }

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
          CategoryDetailOverflowMenu(onDeleteCategoryClick = onDeleteCategoryClick)
        },
      )
    },
  ) { paddingValues ->
    if (isExpanded) {
      CategoryDetailExpandedLayout(
        modifier = Modifier,
        paddingValues = paddingValues,
        state = state,
        pagerState = pagerState,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onShowAddProductDialog = onShowAddProductDialog,
      )
    } else {
      CategoryDetailCompactLayout(
        modifier = Modifier.padding(paddingValues),
        state = state,
        pagerState = pagerState,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onShowAddProductDialog = onShowAddProductDialog,
      )
    }
  }
}

@Composable
private fun CategoryDetailCompactLayout(
  state: CategoryDetailState.Success,
  pagerState: PagerState,
  onNavigateToAddInstance: (productId: String?) -> Unit,
  onShowAddProductDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()

  Column(modifier = modifier.fillMaxSize()) {
    Surface(
      color = CaducityTheme.colorScheme.surfaceContainerHigh,
      shadowElevation = 2.dp,
    ) {
      Column {
        CaducityWeekCalendar(
          modifier = Modifier.padding(horizontal = 16.dp),
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
            val safeSelectedIndex = pagerState.currentPage.coerceIn(0, state.productTabs.size - 1)

            SecondaryScrollableTabRow(
              modifier = Modifier.weight(1f),
              selectedTabIndex = safeSelectedIndex,
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

    // Horizontal Pager for product content
    if (state.productTabs.isNotEmpty()) {
      HorizontalPager(
        state = pagerState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      ) { page ->
        if (page in state.productTabs.indices) {
          val productTab = state.productTabs[page]
          ProductTabContent(
            productTab = productTab,
            onNavigateToAddItem = { _, productId -> onNavigateToAddInstance(productId) },
          )
        }
      }
    }
  }
}

@Composable
private fun CategoryDetailExpandedLayout(
  state: CategoryDetailState.Success,
  pagerState: PagerState,
  paddingValues: PaddingValues,
  onNavigateToAddInstance: (productId: String?) -> Unit,
  onShowAddProductDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()

  Row(
    modifier = modifier
      .fillMaxSize()
      .padding(top = paddingValues.calculateTopPadding())
  ) {
    // Left pane: Month calendar (1/3)
    CategoryDetailCalendarPane(
      modifier = Modifier.weight(0.33f),
      appCalendarConfig = state.appCalendarConfig,
    )

    // Right pane: Tabs + content (2/3)
    Column(
      modifier = Modifier
        .weight(0.67f)
        .fillMaxHeight()
    ) {
      if (state.productTabs.isNotEmpty()) {
        Surface(
          color = CaducityTheme.colorScheme.surfaceContainerHigh,
        ) {
          Row(
            modifier = Modifier.padding(
              top = 8.dp,
              end = 16.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            val safeSelectedIndex = pagerState.currentPage.coerceIn(0, state.productTabs.size - 1)

            SecondaryScrollableTabRow(
              modifier = Modifier.weight(1f),
              selectedTabIndex = safeSelectedIndex,
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

        // Horizontal Pager for product content
        HorizontalPager(
          state = pagerState,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        ) { page ->
          if (page in state.productTabs.indices) {
            val productTab = state.productTabs[page]
            ProductTabContent(
              productTab = productTab,
              onNavigateToAddItem = { _, productId -> onNavigateToAddInstance(productId) },
            )
          }
        }
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

        CategoryDetailSideEffect.ShowDeleteCategoryDialog -> launch {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.category_detail_delete_dialog_title
            ),
            text = AppFeedbackResource.AsResource(
              R.string.category_detail_delete_dialog_message
            ),
            type = AppFeedbackType.Info,
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
