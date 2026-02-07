package com.alorma.caducity.ui.screen.dashboard.filtered

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState

@Composable
fun SideEffectHandler(
  viewModel: FilteredItemsByStatusViewModel,
  bottomSheetState: AppBottomSheetState,
) {
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is FilteredItemsByStatusSideEffect.ShowProductItemsBottomSheet -> {
          bottomSheetState.showProductItemsBottomSheet(
            coroutineScope = this,
            productName = effect.productName,
            items = effect.items,
          )
        }
      }
    }
  }
}
