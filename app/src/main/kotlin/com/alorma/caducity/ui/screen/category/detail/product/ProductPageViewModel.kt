package com.alorma.caducity.ui.screen.category.detail.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.InstanceActionResult
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.GetProductItemsUseCase
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
  productTab: CategoryProductTabUiModel,
  getProductItemsUseCase: GetProductItemsUseCase,
  productPageMapper: ProductPageMapper,
  private val appClock: AppClock,
  private val consumeItemUseCase: ConsumeItemUseCase,
  private val freezeItemUseCase: FreezeItemUseCase,
  private val deleteItemUseCase: DeleteItemUseCase,
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

  private fun LocalDate.toInstant(): Instant {
    return this.atStartOfDayIn(TimeZone.currentSystemDefault())
  }

  private fun emitSideEffect(effect: ProductPageSideEffect) {
    viewModelScope.launch {
      _sideEffect.send(effect)
    }
  }
}
