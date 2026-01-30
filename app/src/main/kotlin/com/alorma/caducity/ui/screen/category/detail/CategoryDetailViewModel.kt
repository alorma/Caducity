package com.alorma.caducity.ui.screen.category.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.InstanceActionResult
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObtainCategoryDetailUseCase
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Instant

class CategoryDetailViewModel(
  private val categoryId: String,
  obtainCategoryDetailUseCase: ObtainCategoryDetailUseCase,
  categoryDetailMapper: CategoryDetailMapper,
  calendarPreferences: CalendarPreferences,
  private val appClock: AppClock,
  private val consumeItemUseCase: ConsumeItemUseCase,
  private val freezeItemUseCase: FreezeItemUseCase,
  private val deleteItemUseCase: DeleteItemUseCase,
  private val createProductUseCase: CreateProductUseCase,
) : ViewModel() {

  private val _sideEffect = Channel<CategoryDetailSideEffect>(Channel.BUFFERED)
  val sideEffect: Flow<CategoryDetailSideEffect> = _sideEffect.receiveAsFlow()

  val state: StateFlow<CategoryDetailState> = combine(
    obtainCategoryDetailUseCase.obtain(categoryId),
    calendarPreferences.state,
  ) { result, calendarConfig ->
    result.fold(
      onSuccess = { category ->
        categoryDetailMapper.mapToCategoryDetail(category, calendarConfig.firstDayOfWeek)
      },
      onFailure = { CategoryDetailState.Error("Not found") },
    )
  }.stateIn(
    viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = CategoryDetailState.Loading
  )

  fun onConsumeItem(item: ItemDetailUiModel) {
    when (item.status) {
      InstanceStatus.ExpiringSoon -> {
        // Only show warning if expiration date is today
        val today = appClock.now().date()
        if (item.expirationDate == today) {
          emitSideEffect(CategoryDetailSideEffect.ShowConsumeExpiredWarning(item))
        } else {
          onConsumeItemConfirmed(item)
        }
      }

      InstanceStatus.Expired -> {
        // Show error dialog for expired items
        emitSideEffect(CategoryDetailSideEffect.ShowConsumeExpiredError(item, item.status))
      }

      InstanceStatus.Fresh -> {
        onConsumeItemConfirmed(item)
      }

      InstanceStatus.Frozen -> {
        // Already consumed or frozen, no action needed
      }
    }
  }

  fun onConsumeItemConfirmed(item: ItemDetailUiModel) {
    viewModelScope.launch {
      when (consumeItemUseCase.forceConsumeItem(item.id)) {
        is InstanceActionResult.Success -> {
          emitSideEffect(CategoryDetailSideEffect.ItemConsumed)
        }

        is InstanceActionResult.Failure -> {
          emitSideEffect(CategoryDetailSideEffect.ConsumeItemFailed)
        }
      }
    }
  }

  fun onFreezeItem(item: ItemDetailUiModel) {
    // Check if item is expired
    if (item.status == InstanceStatus.Expired) {
      emitSideEffect(CategoryDetailSideEffect.FreezeNotAvailable(item.status))
      return
    }

    viewModelScope.launch {
      val expirationInstant = item.expirationDate.toInstant()
      when (freezeItemUseCase.freezeItem(item.id, expirationInstant)) {
        is InstanceActionResult.Success -> {
          emitSideEffect(CategoryDetailSideEffect.ItemFrozen)
        }

        is InstanceActionResult.Failure -> {
          emitSideEffect(CategoryDetailSideEffect.FreezeItemFailed)
        }
      }
    }
  }

  fun onDeleteItem(item: ItemDetailUiModel) {
    viewModelScope.launch {
      val result = deleteItemUseCase.deleteItem(item.id)
      if (result.isSuccess) {
        emitSideEffect(CategoryDetailSideEffect.ItemDeleted)
      } else {
        emitSideEffect(CategoryDetailSideEffect.DeleteItemFailed)
      }
    }
  }

  fun onShowAddProductDialog() {
    emitSideEffect(CategoryDetailSideEffect.ShowAddProductDialog)
  }

  fun onItemClick(item: ItemDetailUiModel) {
    emitSideEffect(CategoryDetailSideEffect.ShowItemActionsBottomSheet(item))
  }

  fun onCreateProduct(productName: String) {
    viewModelScope.launch {
      val result = createProductUseCase.create(categoryId, productName)
      if (result.isSuccess) {
        emitSideEffect(CategoryDetailSideEffect.ProductCreated)
      } else {
        emitSideEffect(CategoryDetailSideEffect.CreateProductFailed)
      }
    }
  }

  private fun LocalDate.toInstant(): Instant {
    return this.atStartOfDayIn(TimeZone.currentSystemDefault())
  }

  private fun emitSideEffect(effect: CategoryDetailSideEffect) {
    viewModelScope.launch {
      _sideEffect.send(effect)
    }
  }

}
