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
import com.alorma.caducity.domain.usecase.GenerateVariantsForProductUseCase
import com.alorma.caducity.domain.usecase.ObtainProductDetailUseCase
import com.alorma.caducity.feature.fakedata.AIPromptDataSource
import com.alorma.caducity.feature.fakedata.models.GeneratedProductVariants
import com.alorma.caducity.feature.fakedata.models.GenerationProgress
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Instant

class ProductDetailViewModel(
  private val productId: String,
  obtainProductDetailUseCase: ObtainProductDetailUseCase,
  productDetailMapper: ProductDetailMapper,
  calendarPreferences: CalendarPreferences,
  private val appClock: AppClock,
  private val consumeInstanceUseCase: ConsumeInstanceUseCase,
  private val freezeInstanceUseCase: FreezeInstanceUseCase,
  private val deleteInstanceUseCase: DeleteInstanceUseCase,
  private val aiPromptDataSource: AIPromptDataSource,
  private val generateVariantsForProductUseCase: GenerateVariantsForProductUseCase,
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

  private val _aiGenerationState = MutableStateFlow(AIGenerationState())
  val aiGenerationState: StateFlow<AIGenerationState> = _aiGenerationState.asStateFlow()

  private var pendingGeneratedVariants: GeneratedProductVariants? = null

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

  fun onGenerateVariantsFromPrompt(prompt: String) {
    val currentState = state.value
    if (currentState !is ProductDetailState.Success) return

    viewModelScope.launch {
      _aiGenerationState.value = AIGenerationState(isGenerating = true)

      // First, get the generated variants from the AI
      val result = aiPromptDataSource.generateVariantsForProduct(
        userPrompt = prompt,
        productName = currentState.product.name
      )

      result.fold(
        onSuccess = { generatedVariants ->
          // Check if anything was generated
          if (generatedVariants.variants.isEmpty() && generatedVariants.standaloneInstances.isEmpty()) {
            _aiGenerationState.value = AIGenerationState(
              isGenerating = false,
              completedResult = GenerationProgress.Completed(
                productsCreated = 0,
                variantsCreated = 0,
                instancesCreated = 0
              )
            )
          } else {
            // Store the variants and show review sheet
            pendingGeneratedVariants = generatedVariants
            _aiGenerationState.value = AIGenerationState(
              isGenerating = false,
              awaitingReview = generatedVariants
            )
          }
        },
        onFailure = { error ->
          val errorMessage = when {
            error.message?.contains("quota", ignoreCase = true) == true ->
              "AI quota exceeded. Try again in 1 minute."
            else -> "Failed to generate variants"
          }
          _aiGenerationState.value = AIGenerationState(
            isGenerating = false,
            error = errorMessage
          )
        }
      )
    }
  }

  fun onConfirmVariants(generatedVariants: GeneratedProductVariants) {
    viewModelScope.launch {
      _aiGenerationState.value = AIGenerationState(isGenerating = true)

      generateVariantsForProductUseCase.confirmAndInsert(
        productId = productId,
        generatedVariants = generatedVariants
      ).collect { progress ->
        when (progress) {
          is GenerationProgress.Started,
          is GenerationProgress.InsertingToDatabase -> {
            _aiGenerationState.value = AIGenerationState(
              isGenerating = true,
              progress = progress
            )
          }
          is GenerationProgress.Completed -> {
            _aiGenerationState.value = AIGenerationState(
              isGenerating = false,
              completedResult = progress
            )
            pendingGeneratedVariants = null
          }
          is GenerationProgress.Failed -> {
            _aiGenerationState.value = AIGenerationState(
              isGenerating = false,
              error = "Failed to add variants"
            )
          }
          else -> {
            // Ignore other progress states
          }
        }
      }
    }
  }

  fun dismissAIError() {
    _aiGenerationState.value = AIGenerationState()
  }

  fun resetAIState() {
    _aiGenerationState.value = AIGenerationState()
    pendingGeneratedVariants = null
  }

  private fun emitSideEffect(effect: ProductDetailSideEffect) {
    viewModelScope.launch {
      _sideEffect.send(effect)
    }
  }

}

data class AIGenerationState(
  val isGenerating: Boolean = false,
  val progress: GenerationProgress? = null,
  val error: String? = null,
  val completedResult: GenerationProgress.Completed? = null,
  val awaitingReview: GeneratedProductVariants? = null
)
