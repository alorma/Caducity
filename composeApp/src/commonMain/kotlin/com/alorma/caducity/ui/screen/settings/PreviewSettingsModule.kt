package com.alorma.caducity.ui.screen.settings

import com.alorma.caducity.version.AppVersionProvider
import org.koin.dsl.module

val previewSettingsModule = module {
  factory {
    object : AppVersionProvider {
      override fun getVersionName(): String = "Preview version"

      override fun getVersionCode(): Int = 1
    }
  }
}