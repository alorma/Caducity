package com.alorma.caducity.feature.appicon

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appIconModule =
  module {
    singleOf(::AndroidAppIconManager) {
      bind<AppIconManager>()
    }
  }
