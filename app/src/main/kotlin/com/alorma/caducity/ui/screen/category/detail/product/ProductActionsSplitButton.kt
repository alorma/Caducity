package com.alorma.caducity.ui.screen.category.detail.product

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.ArrowDown
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.filled.Broom
import com.alorma.caducity.ui.theme.CaducityTheme

@Composable
fun ProductActionsSplitButton(
  onAddItem: () -> Unit,
  onClearItems: () -> Unit,
  onDeleteProduct: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  val checked = remember { mutableStateOf(false) }
  val size = SplitButtonDefaults.MediumContainerHeight
  val buttonColors = ButtonDefaults.filledTonalButtonColors()

  SplitButtonLayout(
    modifier = modifier.heightIn(size),
    leadingButton = {
      SplitButtonDefaults.LeadingButton(
        colors = buttonColors,
        shapes = SplitButtonDefaults.leadingButtonShapesFor(size),
        contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(size),
        onClick = onAddItem,
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
          targetValue = if (checked.value) 180f else 0f,
          label = "Trailing Icon Rotation",
        )

        TooltipBox(
          positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above,
          ),
          tooltip = {
            PlainTooltip {
              Text(stringResource(R.string.category_detail_product_actions_tooltip))
            }
          },
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
          // Clear items option (always shown)
          DropdownMenuItem(
            text = { Text(stringResource(R.string.product_clear_items_action)) },
            onClick = {
              checked.value = false
              onClearItems()
            },
            leadingIcon = {
              Icon(
                imageVector = AppIcons.Outlined.Broom,
                contentDescription = null,
              )
            }
          )

          // Delete product option (only if onDeleteProduct is not null)
          if (onDeleteProduct != null) {
            DropdownMenuItem(
              text = { Text(stringResource(R.string.product_delete_action)) },
              onClick = {
                checked.value = false
                onDeleteProduct()
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
}
