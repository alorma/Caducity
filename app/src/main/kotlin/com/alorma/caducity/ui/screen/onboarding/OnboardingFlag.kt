package com.alorma.caducity.ui.screen.onboarding

import com.alorma.fireandforget.FireAndForget
import com.alorma.fireandforget.FireAndForgetRunner

class OnboardingFlag(
  runner: FireAndForgetRunner,
) : FireAndForget(
    fireAndForgetRunner = runner,
    name = "user_onboarding_v2",
    autoDisable = false,
  )
