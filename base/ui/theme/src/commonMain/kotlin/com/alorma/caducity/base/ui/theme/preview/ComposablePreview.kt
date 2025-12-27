package com.alorma.caducity.base.ui.theme.preview

import androidx.compose.runtime.Composable
import com.alorma.caducity.base.ui.theme.AppTheme
import com.alorma.caducity.base.ui.theme.di.themeModule
import org.koin.compose.KoinApplicationPreview

@Suppress("ModifierRequired")
@Composable
fun AppPreview(
  block: @Composable () -> Unit,
) {
  KoinApplicationPreview(application = { modules(themeModule) }) {
    AppTheme { block() }
  }
}