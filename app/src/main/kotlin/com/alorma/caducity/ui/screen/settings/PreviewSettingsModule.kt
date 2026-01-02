package com.alorma.caducity.ui.screen.settings

import com.alorma.caducity.feature.notification.NotificationDebugHelper
import com.alorma.caducity.feature.version.AppVersionProvider
import org.koin.dsl.module

val previewSettingsModule = module {
  factory<AppVersionProvider> {
    object : AppVersionProvider {
      override fun getVersionName(): String = "Preview version"

      override fun getVersionCode(): Int = 1
    }
  }

  factory<NotificationDebugHelper> {
    object : NotificationDebugHelper {
      override fun triggerImmediateCheck() {

      }
    }
  }
}