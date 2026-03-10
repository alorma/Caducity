package com.alorma.caducity.ui.screen.ai

import com.alorma.caducity.feature.ai.GroceryProposal

sealed interface ChatMessage {
  data class Outgoing(val text: String) : ChatMessage

  data class Proposals(val proposals: List<GroceryProposal>) : ChatMessage

  data object Thinking : ChatMessage

  data object Error : ChatMessage
}
