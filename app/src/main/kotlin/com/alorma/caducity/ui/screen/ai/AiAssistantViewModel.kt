package com.alorma.caducity.ui.screen.ai

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.usecase.AddItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.feature.ai.AiGroceryParser
import com.alorma.caducity.feature.ai.AiProductMatcher
import com.alorma.caducity.feature.ai.MatchResult
import com.alorma.caducity.feature.ai.ModelDownloadState
import com.alorma.caducity.feature.ai.ModelManager
import com.alorma.caducity.ui.base.BaseViewModel
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

class AiAssistantViewModel(
  private val modelManager: ModelManager,
  private val groceryParser: AiGroceryParser,
  private val productMatcher: AiProductMatcher,
  private val addItemToCategoryUseCase: AddItemToCategoryUseCase,
  private val createCategoryUseCase: CreateCategoryUseCase,
  private val createProductUseCase: CreateProductUseCase,
  private val appClock: AppClock,
) : BaseViewModel<AiAssistantNavigation, AiAssistantNavigationSideEffect, Unit>() {
  val modelState: StateFlow<ModelDownloadState> =
    modelManager
      .downloadState()
      .onStart {
        if (!modelManager.isModelReady()) {
          modelManager.startDownload()
        }
      }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = if (modelManager.isModelReady()) ModelDownloadState.Ready else ModelDownloadState.Idle,
      )

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  fun send(input: String) {
    if (input.isBlank()) return

    _messages.update { it + ChatMessage.Outgoing(input) + ChatMessage.Thinking }

    viewModelScope.launch {
      val todayIso = todayIsoString()
      val proposals = groceryParser.parse(input, todayIso)

      val proposalUiModels = proposals.map { proposal ->
        val matchResult = productMatcher.match(proposal)
        ProposalUiModel(proposal = proposal, matchResult = matchResult)
      }

      _messages.update { messages ->
        val withoutThinking = messages.filterNot { it is ChatMessage.Thinking }
        if (proposalUiModels.isEmpty()) {
          withoutThinking + ChatMessage.Error
        } else {
          withoutThinking + ChatMessage.Proposals(proposalUiModels)
        }
      }
    }
  }

  fun onAddToProduct(proposalUiModel: ProposalUiModel) {
    val match = proposalUiModel.matchResult as? MatchResult.Match ?: return
    viewModelScope.launch {
      val expirationDate = parseExpirationDate(proposalUiModel.proposal.expirationDate) ?: return@launch
      repeat(proposalUiModel.proposal.quantity) {
        addItemToCategoryUseCase.addItem(
          categoryId = match.category.id,
          identifier = proposalUiModel.proposal.productName,
          productId = match.product.id,
          expirationDate = expirationDate,
        )
      }
      markProposalDone(proposalUiModel)
    }
  }

  fun onCreateNew(proposalUiModel: ProposalUiModel) {
    viewModelScope.launch {
      val expirationDate = parseExpirationDate(proposalUiModel.proposal.expirationDate) ?: return@launch
      val categoryResult = createCategoryUseCase.createCategory(
        name = proposalUiModel.proposal.category,
        description = "",
        items = emptyList(),
      )
      val categoryId = categoryResult.getOrNull() ?: return@launch
      val productResult = createProductUseCase.create(
        categoryId = categoryId,
        name = proposalUiModel.proposal.productName,
      )
      val productId = productResult.getOrNull()?.id ?: return@launch
      repeat(proposalUiModel.proposal.quantity) {
        addItemToCategoryUseCase.addItem(
          categoryId = categoryId,
          identifier = proposalUiModel.proposal.productName,
          productId = productId,
          expirationDate = expirationDate,
        )
      }
      markProposalDone(proposalUiModel)
    }
  }

  private fun markProposalDone(proposalUiModel: ProposalUiModel) {
    _messages.update { messages ->
      messages.map { message ->
        if (message is ChatMessage.Proposals) {
          val updatedProposals = message.proposals.map { item ->
            if (item === proposalUiModel) item.copy(done = true) else item
          }
          message.copy(proposals = updatedProposals)
        } else {
          message
        }
      }
    }
  }

  @OptIn(FormatStringsInDatetimeFormats::class)
  private fun parseExpirationDate(isoDate: String): Instant? =
    try {
      val localDate = LocalDate.Format { byUnicodePattern("yyyy-MM-dd") }.parse(isoDate)
      localDate.atStartOfDayIn(TimeZone.currentSystemDefault())
    } catch (_: Exception) {
      null
    }

  @OptIn(FormatStringsInDatetimeFormats::class)
  private fun todayIsoString(): String {
    val today = appClock.nowDate()
    return LocalDate.Format { byUnicodePattern("yyyy-MM-dd") }.format(today)
  }

  override fun navigate(navigation: AiAssistantNavigation) {
    when (navigation) {
      AiAssistantNavigation.Cancel -> emitNavigationSideEffect(AiAssistantNavigationSideEffect.NavigateBack)
    }
  }
}
