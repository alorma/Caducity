package com.alorma.caducity.feature.consent

import com.alorma.fireandforget.FireAndForget
import com.alorma.fireandforget.FireAndForgetRunner

/**
 * Flag to track whether the user has been shown the consent screen.
 * This ensures the consent screen is shown only once.
 * The flag is disabled when the user provides their consent choice.
 */
class ConsentFlag(
  runner: FireAndForgetRunner,
) : FireAndForget(
    fireAndForgetRunner = runner,
    name = "analytics_consent",
    autoDisable = true,
  )
