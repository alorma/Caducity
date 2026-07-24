package com.alorma.caducity.ui.screen.ai

sealed interface ChatMessage {
  data class Outgoing(
    val text: String,
  ) : ChatMessage

  data class Proposals(
    val proposals: List<ProposalUiModel>,
  ) : ChatMessage

  data object Thinking : ChatMessage

  data class Error(
    val reason: Reason,
    val debugDetail: String? = null,
  ) : ChatMessage {
    enum class Reason {
      /** The model ran but found no grocery products in the message. */
      NoGroceries,

      /** The on-device model has not finished downloading. */
      ModelNotReady,

      /** Generation or response parsing failed. */
      Failed,
    }
  }
}
