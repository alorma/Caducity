package com.alorma.caducity.ui.screen.ai

sealed interface ChatMessage {
  data class Outgoing(val text: String) : ChatMessage

  data class Proposals(val proposals: List<ProposalUiModel>) : ChatMessage

  data object Thinking : ChatMessage

  data object Error : ChatMessage
}
