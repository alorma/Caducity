package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.config.navigation.BottomSheetSceneStrategy

@Composable
fun ProductDetailContainer(
  productId: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val productDetailBackStack = retain {
    mutableStateListOf<NavKey>(ProductDetailRoutes.Root(productId))
  }

  val bottomSheetStrategy = remember {
    BottomSheetSceneStrategy<NavKey>()
  }

  NavDisplay(
    modifier = modifier,
    backStack = productDetailBackStack,
    sceneStrategy = bottomSheetStrategy,
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider = entryProvider {
      entry<ProductDetailRoutes.Root> {
        ProductDetailScreen(
          productId = it.productId,
          onBack = onBack,
          onNavigateToAddInstance = {
            productDetailBackStack.add(ProductDetailRoutes.AddInstance(productId))
          },
        )
      }
      entry<ProductDetailRoutes.AddInstance> {
        ProductDetailAddInstanceScreen(
          productId = it.productId,
          onClose = { productDetailBackStack.removeLastOrNull() },
        )
      }
    },
  )
}
