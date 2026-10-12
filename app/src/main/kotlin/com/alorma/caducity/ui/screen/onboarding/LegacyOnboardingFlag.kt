package com.alorma.caducity.ui.screen.onboarding

import com.alorma.fireandforget.FireAndForget
import com.alorma.fireandforget.FireAndForgetRunner

/** The flag of the onboarding before 1.0.0. Disabled means the user already used the app; it is only read, never changed. */
class LegacyOnboardingFlag(
  runner: FireAndForgetRunner,
) : FireAndForget(
    fireAndForgetRunner = runner,
    name = "user_onboarding",
    autoDisable = false,
  )
