package com.alorma.caducity.ui.screen.category.detail.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.filled.Broom
import com.alorma.caducity.base.ui.icons.outlined.MoveGroup
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.category.detail.CategoryProductTabUiModel
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.alorma.caducity.feature.tracking.ProductDeleteBottomSheetScreen
import com.alorma.caducity.feature.tracking.ProductClearItemsBottomSheetScreen
import com.alorma.caducity.feature.tracking.TrackScreen

internal fun AppBottomSheetState.showDeleteProductWithItemsBottomSheet(
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
      DeleteProductWithItemsBottomSheetContent(
        itemCount = itemCount,
        availableProducts = availableProducts,
        showProductSelection = showProductSelection,
        onShowProductSelection = { showProductSelection = it },
        onMoveToStandalone = onMoveToStandalone,
        onMoveToProduct = onMoveToProduct,
        onCascadeDelete = onCascadeDelete,
      )
    }
  }
}

@Composable
private fun DeleteProductWithItemsBottomSheetContent(
  itemCount: Int,
  availableProducts: List<CategoryProductTabUiModel>,
  showProductSelection: Boolean,
  onShowProductSelection: (Boolean) -> Unit,
  onMoveToStandalone: () -> Unit,
  onMoveToProduct: (String) -> Unit,
  onCascadeDelete: () -> Unit,
) {
  TrackScreen(screen = ProductDeleteBottomSheetScreen())
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
                icon = {
                  Icon(
                    imageVector = AppIcons.Outlined.MoveGroup,
                    contentDescription = null,
                  )
                },
                title = stringResource(R.string.product_delete_option_move_to_standalone),
                subtitle = stringResource(R.string.product_delete_option_move_to_standalone_desc),
                onClick = onMoveToStandalone,
                position = standalonePosition,
              )

              // Option 2: Move to another product (only if there are other products)
              if (hasProductOption) {
                StyledSettingsCard(
                  icon = {
                    Icon(
                      imageVector = AppIcons.Outlined.MoveGroup,
                      contentDescription = null,
                    )
                  },
                  title = stringResource(R.string.product_delete_option_move_to_product),
                  subtitle = stringResource(R.string.product_delete_option_move_to_product_desc),
                  onClick = { onShowProductSelection(true) },
                  position = productPosition,
                )
              }

              // Option 3: Delete all items (cascade delete)
              StyledSettingsCard(
                icon = {
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
              IconButton(onClick = { onShowProductSelection(false) }) {
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

internal fun AppBottomSheetState.showClearItemsBottomSheet(
  coroutineScope: CoroutineScope,
  onClearConsumed: () -> Unit,
  onClearAll: () -> Unit,
) {
  coroutineScope.launch {
    show {
      ClearItemsBottomSheetContent(
        onClearConsumed = onClearConsumed,
        onClearAll = onClearAll,
      )
    }
  }
}

@Composable
private fun ClearItemsBottomSheetContent(
  onClearConsumed: () -> Unit,
  onClearAll: () -> Unit,
) {
  TrackScreen(screen = ProductClearItemsBottomSheetScreen())
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
            colors = SettingsTileDefaults.colors(
              containerColor = CaducityTheme.colorScheme.errorContainer,
              titleColor = CaducityTheme.colorScheme.onErrorContainer,
              subtitleColor = CaducityTheme.colorScheme.onErrorContainer,
              iconColor = CaducityTheme.colorScheme.onErrorContainer,
              actionColor = CaducityTheme.colorScheme.onErrorContainer,
            ),
          )
        }
      }
}
