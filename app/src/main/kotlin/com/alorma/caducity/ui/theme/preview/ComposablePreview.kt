package com.alorma.caducity.ui.theme.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.alorma.caducity.di.appModule
import com.alorma.caducity.ui.theme.AppTheme
import com.alorma.caducity.ui.theme.LocalSystemBarsAppearance
import com.alorma.caducity.ui.theme.SystemBarsAppearanceNoOp
import org.koin.compose.KoinApplicationPreview
import org.koin.core.module.Module
import org.koin.dsl.module

@Suppress("ModifierRequired")
@Composable
fun AppPreview(
  module: Module = module { },
  block: @Composable () -> Unit,
) {
  KoinApplicationPreview(application = {
    modules(appModule, module)
  }) {
    CompositionLocalProvider(
      LocalSystemBarsAppearance provides SystemBarsAppearanceNoOp,
    ) {
      AppTheme { block() }
    }
  }
}