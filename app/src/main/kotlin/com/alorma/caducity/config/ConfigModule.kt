package com.alorma.caducity.config

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.clock.KotlinAppClock
import com.alorma.caducity.config.language.AndroidLanguageManager
import com.alorma.caducity.config.language.LanguageManager
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.config.version.AndroidAppVersionProvider
import com.alorma.caducity.config.version.AppVersionProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat
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

  single<DateTimeFormat<LocalDate>>(qualifier = ConfigQualifier.DateFormat.HumanReadable) {
    LocalDate.Format {
      day()
      chars("/")
      monthNumber()
      chars("/")
      year()
    }
  }
  single<DateTimeFormat<LocalDateTime>>(qualifier = ConfigQualifier.DateFormat.BackupName) {
    LocalDateTime.Format {
      day()
      chars("_")
      monthNumber()
      chars("_")
      year()
      hour()
      chars("_")
      minute()
      chars("_")
    }
  }
}