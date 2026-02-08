package com.alorma.caducity.feature.consent

import com.alorma.fireandforget.FireAndForget
import com.alorma.fireandforget.FireAndForgetRunner

/**
 * Flag to track whether the user has been shown the consent screen.
 * This ensures the consent screen is shown only once.
 */
class ConsentFlag(runner: FireAndForgetRunner) : FireAndForget(
  fireAndForgetRunner = runner,
  name = "analytics_consent",
  autoDisable = true, // Automatically disable after first use
)
