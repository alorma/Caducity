package com.alorma.caducity.ui.screen.product.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.InstanceActionResult
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.usecase.ConsumeInstanceUseCase
import com.alorma.caducity.domain.usecase.DeleteInstanceUseCase
import com.alorma.caducity.domain.usecase.FreezeInstanceUseCase
import com.alorma.caducity.domain.usecase.ObtainProductDetailUseCase
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

class ProductDetailViewModel(
  productId: String,
  obtainProductDetailUseCase: ObtainProductDetailUseCase,
  productDetailMapper: ProductDetailMapper,
  calendarPreferences: CalendarPreferences,
  private val appClock: AppClock,
  private val consumeInstanceUseCase: ConsumeInstanceUseCase,
  private val freezeInstanceUseCase: FreezeInstanceUseCase,
  private val deleteInstanceUseCase: DeleteInstanceUseCase,
) : ViewModel() {

  private val _sideEffect = Channel<ProductDetailSideEffect>(Channel.BUFFERED)
  val sideEffect: Flow<ProductDetailSideEffect> = _sideEffect.receiveAsFlow()

  val state: StateFlow<ProductDetailState> = combine(
    obtainProductDetailUseCase.obtain(productId),
    calendarPreferences.state,
  ) { result, calendarConfig ->
    result.fold(
      onSuccess = { product ->
        productDetailMapper.mapToProductDetail(product, calendarConfig.firstDayOfWeek)
      },
      onFailure = { ProductDetailState.Error("Not found") },
    )
  }.stateIn(
    viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ProductDetailState.Loading
  )

  fun onConsumeInstance(instance: ProductInstanceDetailUiModel) {
    when (instance.status) {
      InstanceStatus.ExpiringSoon -> {
        // Only show warning if expiration date is today
        val today = appClock.now().date()
        if (instance.expirationDate == today) {
          emitSideEffect(ProductDetailSideEffect.ShowConsumeExpiredWarning(instance))
        } else {
          onConsumeInstanceConfirmed(instance)
        }
      }

      InstanceStatus.Expired -> {
        // Show error dialog for expired items
        emitSideEffect(ProductDetailSideEffect.ShowConsumeExpiredError(instance, instance.status))
      }

      InstanceStatus.Fresh -> {
        onConsumeInstanceConfirmed(instance)
      }

      InstanceStatus.Frozen -> {
        // Already consumed or frozen, no action needed
      }
    }
  }

  fun onConsumeInstanceConfirmed(instance: ProductInstanceDetailUiModel) {
    viewModelScope.launch {
      when (consumeInstanceUseCase.forceConsumeInstance(instance.id)) {
        is InstanceActionResult.Success -> {
          emitSideEffect(ProductDetailSideEffect.InstanceConsumed)
        }
        is InstanceActionResult.Failure -> {
          emitSideEffect(ProductDetailSideEffect.ConsumeInstanceFailed)
        }
      }
    }
  }

  fun onFreezeInstance(instance: ProductInstanceDetailUiModel) {
    // Check if instance is expired
    if (instance.status == InstanceStatus.Expired) {
      emitSideEffect(ProductDetailSideEffect.FreezeNotAvailable(instance.status))
      return
    }

    viewModelScope.launch {
      val expirationInstant = instance.expirationDate.toInstant()
      when (freezeInstanceUseCase.freezeInstance(instance.id, expirationInstant)) {
        is InstanceActionResult.Success -> {
          emitSideEffect(ProductDetailSideEffect.InstanceFrozen)
        }
        is InstanceActionResult.Failure -> {
          emitSideEffect(ProductDetailSideEffect.FreezeInstanceFailed)
        }
      }
    }
  }

  fun onDeleteInstance(instance: ProductInstanceDetailUiModel) {
    viewModelScope.launch {
      val result = deleteInstanceUseCase.deleteInstance(instance.id)
      if (result.isSuccess) {
        emitSideEffect(ProductDetailSideEffect.InstanceDeleted)
      } else {
        emitSideEffect(ProductDetailSideEffect.DeleteInstanceFailed)
      }
    }
  }

  private fun LocalDate.toInstant(): Instant {
    return this.atStartOfDayIn(TimeZone.currentSystemDefault())
  }

  private fun emitSideEffect(effect: ProductDetailSideEffect) {
    viewModelScope.launch {
      _sideEffect.send(effect)
    }
  }
}
