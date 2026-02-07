package com.alorma.caducity.ui.screen.category.detail.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.ProductDeletionStrategy
import com.alorma.caducity.domain.usecase.ClearProductItemsUseCase
import com.alorma.caducity.domain.usecase.DeleteProductUseCase
import com.alorma.caducity.domain.usecase.GetCategoryProductsUseCase
import com.alorma.caducity.domain.usecase.GetProductItemsUseCase
import com.alorma.caducity.ui.screen.category.detail.CategoryProductTabUiModel
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductPageViewModel(
  private val productTab: CategoryProductTabUiModel,
  private val getCategoryProductsUseCase: GetCategoryProductsUseCase,
  getProductItemsUseCase: GetProductItemsUseCase,
  productPageMapper: ProductPageMapper,
  private val deleteProductUseCase: DeleteProductUseCase,
  private val clearProductItemsUseCase: ClearProductItemsUseCase,
) : ViewModel() {

  private val _sideEffect = Channel<ProductPageSideEffect>(Channel.BUFFERED)
  val sideEffect: Flow<ProductPageSideEffect> = _sideEffect.receiveAsFlow()

  val state: StateFlow<ProductPageState> = getProductItemsUseCase
    .obtain(productTab.categoryId, productTab.id)
    .map<_, ProductPageState> { productItems ->
      productPageMapper.mapToUiModel(productItems)
    }
    .catch { error ->
      emit(ProductPageState.Error(error.message ?: "Unknown error"))
    }
    .stateIn(
      viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = ProductPageState.Loading
    )

  fun onItemClick(item: ItemDetailUiModel) {
    emitSideEffect(ProductPageSideEffect.ShowItemActionsBottomSheet(item))
  }

  fun onAddItemClick() {
    emitSideEffect(
      ProductPageSideEffect.NavigateToAddItem(
        categoryId = productTab.categoryId,
        productId = productTab.id
      )
    )
  }

  fun onDeleteProductClick() {
    val productId = productTab.id ?: return // "Other" tab cannot delete
    viewModelScope.launch {
      val activeItemCount = deleteProductUseCase.getActiveItemCount(productId)
      if (activeItemCount > 0) {
        // Get available products from use case
        val products = getCategoryProductsUseCase
          .obtain(productTab.categoryId)
          .first()

        val availableProducts = products
          .filter { it.id != productId }
          .map { product ->
            CategoryProductTabUiModel(
              id = product.id,
              categoryId = productTab.categoryId,
              name = product.name,
            )
          }
        emitSideEffect(
          ProductPageSideEffect.ShowDeleteProductWithItemsDialog(
            productId = productId,
            activeItemCount = activeItemCount,
            availableProducts = availableProducts,
            onDeleteProduct = ::onDeleteProduct,
          )
        )
      } else {
        emitSideEffect(
          ProductPageSideEffect.ShowDeleteProductDialog(
            productId = productId,
            onDeleteProduct = ::onDeleteProduct,
          )
        )
      }
    }
  }

  fun onDeleteProduct(productId: String, strategy: ProductDeletionStrategy) {
    viewModelScope.launch {
      val result = deleteProductUseCase.delete(productId, strategy)
      if (result.isSuccess) {
        emitSideEffect(ProductPageSideEffect.ProductDeleted)
      } else {
        emitSideEffect(ProductPageSideEffect.DeleteProductFailed)
      }
    }
  }

  fun onClearProductItemsClick() {
    emitSideEffect(
      ProductPageSideEffect.ShowClearProductItemsDialog(
        productId = productTab.id,
        onClearProductItems = ::onClearProductItems,
      )
    )
  }

  fun onClearProductItems(productId: String?, clearAll: Boolean) {
    viewModelScope.launch {
      val result = if (clearAll) {
        clearProductItemsUseCase.clearAllItems(productTab.categoryId, productId)
      } else {
        clearProductItemsUseCase.clearConsumedItems(productTab.categoryId, productId)
      }

      if (result.isSuccess) {
        emitSideEffect(ProductPageSideEffect.ItemsCleared)
      } else {
        emitSideEffect(ProductPageSideEffect.ClearItemsFailed)
      }
    }
  }

  private fun emitSideEffect(effect: ProductPageSideEffect) {
    viewModelScope.launch {
      _sideEffect.send(effect)
    }
  }
}
