package com.alorma.caducity.config

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.clock.KotlinAppClock
import com.alorma.caducity.config.language.AndroidLanguageManager
import com.alorma.caducity.config.language.LanguageManager
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.config.version.AndroidAppVersionProvider
import com.alorma.caducity.config.version.AppVersionProvider
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val configModule = module {
  single<AppClock> { KotlinAppClock() }

  singleOf(::AndroidAppVersionProvider) {
    bind<AppVersionProvider>()
  }


  singleOf(::AndroidLanguageManager) {
    bind<LanguageManager>()
  }

  singleOf(::LocalizedDateFormatter)

}