package com.alorma.caducity.ui.screen.ai

import com.alorma.caducity.ui.base.BaseViewModel

class AiAssistantViewModel :
  BaseViewModel<AiAssistantNavigation, AiAssistantNavigationSideEffect, Unit>() {
  override fun navigate(navigation: AiAssistantNavigation) {
    when (navigation) {
      AiAssistantNavigation.Cancel -> emitNavigationSideEffect(AiAssistantNavigationSideEffect.NavigateBack)
    }
  }
}
