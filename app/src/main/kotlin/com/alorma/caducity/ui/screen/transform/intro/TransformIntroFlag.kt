package com.alorma.caducity.ui.screen.transform.intro

import com.alorma.fireandforget.FireAndForget
import com.alorma.fireandforget.FireAndForgetRunner

/**
 * «Novetat Transforma» modal: enabled means it is still pending. Whoever updates the app has it enabled and sees the
 * modal once; the onboarding disables it when it finishes, so new installs never see it (they got the onboarding step).
 */
class TransformIntroFlag(
  runner: FireAndForgetRunner,
) : FireAndForget(
    fireAndForgetRunner = runner,
    name = "transform_intro",
    autoDisable = false,
  )
