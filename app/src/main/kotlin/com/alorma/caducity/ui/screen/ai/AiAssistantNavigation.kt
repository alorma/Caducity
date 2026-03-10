package com.alorma.caducity.ui.screen.ai

sealed interface AiAssistantNavigation {
  data object Cancel : AiAssistantNavigation
}

sealed interface AiAssistantNavigationSideEffect {
  data object NavigateBack : AiAssistantNavigationSideEffect
}
