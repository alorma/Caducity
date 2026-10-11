package com.alorma.caducity.ui.screen.home

import com.alorma.fireandforget.FireAndForget
import com.alorma.fireandforget.FireAndForgetRunner

/** Dismissable hint on the empty Home that suggests adding the widget. */
class WidgetTipFlag(
  runner: FireAndForgetRunner,
) : FireAndForget(
    fireAndForgetRunner = runner,
    name = "widget_tip",
    autoDisable = false,
  )
