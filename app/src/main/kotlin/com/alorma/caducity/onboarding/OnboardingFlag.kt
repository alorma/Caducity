package com.alorma.caducity.onboarding

import com.alorma.fireandforget.FireAndForget
import com.alorma.fireandforget.FireAndForgetRunner

class OnboardingFlag(runner: FireAndForgetRunner) : FireAndForget(
  fireAndForgetRunner = runner,
  name = "user_onboarding",
  defaultValue = true // true means onboarding should be shown by default for new users
)
