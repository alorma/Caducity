package com.alorma.caducity.ui.screen.category.detail.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.InstanceActionResult
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.ProductDeletionStrategy
import com.alorma.caducity.domain.usecase.ClearProductItemsUseCase
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.DeleteProductUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.GetProductItemsUseCase
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailState
import com.alorma.caducity.ui.screen.category.detail.CategoryProductTabUiModel
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Instant

class ProductPageViewModel(
  private val productTab: CategoryProductTabUiModel,
  private val categoryState: StateFlow<CategoryDetailState>,
  getProductItemsUseCase: GetProductItemsUseCase,
  productPageMapper: ProductPageMapper,
  private val appClock: AppClock,
  private val consumeItemUseCase: ConsumeItemUseCase,
  private val freezeItemUseCase: FreezeItemUseCase,
  private val deleteItemUseCase: DeleteItemUseCase,
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

  fun onConsumeItem(item: ItemDetailUiModel) {
    when (item.status) {
      ItemStatus.ExpiringSoon -> {
        // Only show warning if expiration date is today
        val today = appClock.now().date()
        if (item.expirationDate == today) {
          emitSideEffect(ProductPageSideEffect.ShowConsumeExpiredWarning(item))
        } else {
          onConsumeItemConfirmed(item)
        }
      }

      ItemStatus.Expired -> {
        // Show error dialog for expired items
        emitSideEffect(ProductPageSideEffect.ShowConsumeExpiredError(item, item.status))
      }

      ItemStatus.Fresh -> {
        onConsumeItemConfirmed(item)
      }

      ItemStatus.Frozen -> {
        // Already consumed or frozen, no action needed
      }

      ItemStatus.Consumed -> {
        // Already consumed, no action needed
      }
    }
  }

  fun onConsumeItemConfirmed(item: ItemDetailUiModel) {
    viewModelScope.launch {
      when (consumeItemUseCase.forceConsumeItem(item.id)) {
        is InstanceActionResult.Success -> {
          emitSideEffect(ProductPageSideEffect.ItemConsumed)
        }

        is InstanceActionResult.Failure -> {
          emitSideEffect(ProductPageSideEffect.ConsumeItemFailed)
        }
      }
    }
  }

  fun onFreezeItem(item: ItemDetailUiModel) {
    // Check if item is expired
    if (item.status == ItemStatus.Expired) {
      emitSideEffect(ProductPageSideEffect.FreezeNotAvailable(item.status))
      return
    }

    viewModelScope.launch {
      val expirationInstant = item.expirationDate.toInstant()
      when (freezeItemUseCase.freezeItem(item.id, expirationInstant)) {
        is InstanceActionResult.Success -> {
          emitSideEffect(ProductPageSideEffect.ItemFrozen)
        }

        is InstanceActionResult.Failure -> {
          emitSideEffect(ProductPageSideEffect.FreezeItemFailed)
        }
      }
    }
  }

  fun onDeleteItem(item: ItemDetailUiModel) {
    viewModelScope.launch {
      val result = deleteItemUseCase.deleteItem(item.id)
      if (result.isSuccess) {
        emitSideEffect(ProductPageSideEffect.ItemDeleted)
      } else {
        emitSideEffect(ProductPageSideEffect.DeleteItemFailed)
      }
    }
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
        // Get available products from observed category state
        val availableProducts = categoryState.value.let { state ->
          if (state is CategoryDetailState.Success) {
            state.productTabs.filter { it.id != null && it.id != productId }
          } else emptyList()
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

  private fun LocalDate.toInstant(): Instant {
    return this.atStartOfDayIn(TimeZone.currentSystemDefault())
  }

  private fun emitSideEffect(effect: ProductPageSideEffect) {
    viewModelScope.launch {
      _sideEffect.send(effect)
    }
  }
}
