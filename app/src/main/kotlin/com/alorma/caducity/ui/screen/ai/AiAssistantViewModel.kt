package com.alorma.caducity.ui.screen.ai

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.usecase.AddItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.feature.ai.AiGroceryParser
import com.alorma.caducity.feature.ai.AiProductMatcher
import com.alorma.caducity.feature.ai.MatchResult
import com.alorma.caducity.feature.ai.ModelDownloadState
import com.alorma.caducity.feature.ai.ModelManager
import com.alorma.caducity.feature.ai.priority
import com.alorma.caducity.ui.base.BaseViewModel
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AiAssistantViewModel(
  private val modelManager: ModelManager,
  private val groceryParser: AiGroceryParser,
  private val productMatcher: AiProductMatcher,
  private val categoryDataSource: CategoryDataSource,
  private val addItemToCategoryUseCase: AddItemToCategoryUseCase,
  private val createCategoryUseCase: CreateCategoryUseCase,
  private val createProductUseCase: CreateProductUseCase,
) : BaseViewModel<AiAssistantNavigation, AiAssistantNavigationSideEffect, AiAssistantSideEffect>() {
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
      val categoryNames =
        categoryDataSource
          .getCategories()
          .first()
          .map { it.category.name }
      val proposals = groceryParser.parse(input, categoryNames)

      val proposalUiModels =
        proposals
          .map { proposal ->
            val matchResult = productMatcher.match(proposal)
            ProposalUiModel(proposal = proposal, matchResult = matchResult)
          }.groupBy { it.proposal.productName.lowercase() }
          .map { (_, duplicates) ->
            // Keep the best match per product name: Match > CategoryMatch > NoMatch
            duplicates.minByOrNull { it.matchResult.priority } ?: duplicates.first()
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
    emitSideEffect(AiAssistantSideEffect.ShowDatePicker(proposalUiModel))
  }

  fun onAddToCategory(proposalUiModel: ProposalUiModel) {
    emitSideEffect(AiAssistantSideEffect.ShowDatePicker(proposalUiModel))
  }

  fun onCreateNew(proposalUiModel: ProposalUiModel) {
    emitSideEffect(AiAssistantSideEffect.ShowDatePicker(proposalUiModel))
  }

  fun onDateConfirmed(
    proposalUiModel: ProposalUiModel,
    expirationDate: Instant,
  ) {
    viewModelScope.launch {
      when (val match = proposalUiModel.matchResult) {
        is MatchResult.Match -> {
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
        is MatchResult.CategoryMatch -> {
          val productResult =
            createProductUseCase.create(
              categoryId = match.category.id,
              name = proposalUiModel.proposal.productName,
            )
          val productId = productResult.getOrNull()?.id
          if (productId == null) {
            Timber
              .tag(
                "AiViewModel",
              ).e(
                "onDateConfirmed: failed to create product '%s': %s",
                proposalUiModel.proposal.productName,
                productResult.exceptionOrNull(),
              )
            return@launch
          }
          repeat(proposalUiModel.proposal.quantity) {
            addItemToCategoryUseCase.addItem(
              categoryId = match.category.id,
              identifier = proposalUiModel.proposal.productName,
              productId = productId,
              expirationDate = expirationDate,
            )
          }
          markProposalDone(proposalUiModel)
        }
        MatchResult.NoMatch -> {
          val existingCategories = categoryDataSource.getCategories().first()
          val existingCategory =
            existingCategories.firstOrNull {
              it.category.name.equals(proposalUiModel.proposal.category, ignoreCase = true)
            }
          val categoryId =
            if (existingCategory != null) {
              existingCategory.category.id
            } else {
              val categoryResult =
                createCategoryUseCase.createCategory(
                  name = proposalUiModel.proposal.category,
                  description = "",
                  items = emptyList(),
                )
              val id = categoryResult.getOrNull()
              if (id == null) {
                Timber
                  .tag(
                    "AiViewModel",
                  ).e(
                    "onDateConfirmed: failed to create category '%s': %s",
                    proposalUiModel.proposal.category,
                    categoryResult.exceptionOrNull(),
                  )
                return@launch
              }
              id
            }
          val productResult =
            createProductUseCase.create(
              categoryId = categoryId,
              name = proposalUiModel.proposal.productName,
            )
          val productId = productResult.getOrNull()?.id
          if (productId == null) {
            Timber
              .tag(
                "AiViewModel",
              ).e(
                "onDateConfirmed: failed to create product '%s': %s",
                proposalUiModel.proposal.productName,
                productResult.exceptionOrNull(),
              )
            return@launch
          }
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
    }
  }

  private fun markProposalDone(proposalUiModel: ProposalUiModel) {
    _messages.update { messages ->
      messages.map { message ->
        if (message is ChatMessage.Proposals) {
          val updatedProposals =
            message.proposals.map { item ->
              if (item === proposalUiModel) item.copy(done = true) else item
            }
          message.copy(proposals = updatedProposals)
        } else {
          message
        }
      }
    }
  }

  override fun navigate(navigation: AiAssistantNavigation) {
    when (navigation) {
      AiAssistantNavigation.Cancel -> emitNavigationSideEffect(AiAssistantNavigationSideEffect.NavigateBack)
    }
  }
}
