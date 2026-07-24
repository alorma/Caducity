package com.alorma.caducity.ui.screen.ai

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.feature.ai.AiGroceryParser
import com.alorma.caducity.feature.ai.AiProductMatcher
import com.alorma.caducity.feature.ai.CommitProposalUseCase
import com.alorma.caducity.feature.ai.GroceryParseResult
import com.alorma.caducity.feature.ai.ModelDownloadState
import com.alorma.caducity.feature.ai.ModelManager
import com.alorma.caducity.feature.ai.priority
import com.alorma.caducity.ui.base.BaseViewModel
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
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
  private val commitProposalUseCase: CommitProposalUseCase,
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

  @OptIn(ExperimentalUuidApi::class)
  fun send(input: String) {
    if (input.isBlank()) return

    _messages.update { it + ChatMessage.Outgoing(input) + ChatMessage.Thinking }

    viewModelScope.launch {
      val categoryNames =
        categoryDataSource
          .getCategories()
          .first()
          .map { it.category.name }

      val outcome =
        when (val result = groceryParser.parse(input, categoryNames)) {
          is GroceryParseResult.Success -> {
            val proposalUiModels =
              result.proposals
                .map { proposal ->
                  ProposalUiModel(
                    id = Uuid.random().toString(),
                    proposal = proposal,
                    matchResult = productMatcher.match(proposal),
                  )
                }.groupBy { it.proposal.productName.lowercase() }
                .map { (_, duplicates) ->
                  // Keep the best match per product name: Match > CategoryMatch > NoMatch
                  duplicates.minByOrNull { it.matchResult.priority } ?: duplicates.first()
                }
            ChatMessage.Proposals(proposalUiModels)
          }
          GroceryParseResult.NoGroceriesFound -> ChatMessage.Error(ChatMessage.Error.Reason.NoGroceries)
          GroceryParseResult.ModelNotReady -> ChatMessage.Error(ChatMessage.Error.Reason.ModelNotReady)
          GroceryParseResult.Failed -> ChatMessage.Error(ChatMessage.Error.Reason.Failed)
        }

      _messages.update { messages ->
        messages.filterNot { it is ChatMessage.Thinking } + outcome
      }
    }
  }

  fun onProposalAction(proposalUiModel: ProposalUiModel) {
    emitSideEffect(AiAssistantSideEffect.ShowDatePicker(proposalUiModel))
  }

  fun onDateConfirmed(
    proposalUiModel: ProposalUiModel,
    expirationDate: Instant,
  ) {
    viewModelScope.launch {
      commitProposalUseCase
        .commit(
          proposal = proposalUiModel.proposal,
          matchResult = proposalUiModel.matchResult,
          expirationDate = expirationDate,
        ).onSuccess { markProposalDone(proposalUiModel) }
        .onFailure { error ->
          Timber.tag("AiViewModel").e(error, "Failed to commit proposal '%s'", proposalUiModel.proposal.productName)
          emitSideEffect(AiAssistantSideEffect.ShowCommitError)
        }
    }
  }

  private fun markProposalDone(proposalUiModel: ProposalUiModel) {
    _messages.update { messages ->
      messages.map { message ->
        if (message is ChatMessage.Proposals) {
          val updatedProposals =
            message.proposals.map { item ->
              if (item.id == proposalUiModel.id) item.copy(done = true) else item
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
