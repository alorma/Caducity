package com.alorma.caducity.config

import com.alorma.caducity.BuildConfig
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.clock.KotlinAppClock
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.config.remoteconfig.DebugRemoteConfigRunner
import com.alorma.caducity.config.remoteconfig.FirebaseRemoteConfigProvider
import com.alorma.caducity.config.remoteconfig.RemoteConfig
import com.alorma.caducity.config.remoteconfig.RemoteConfigRunner
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.config.version.AndroidAppVersionProvider
import com.alorma.caducity.config.version.AppVersionProvider
import com.alorma.caducity.ui.adaptive.TabletModeRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
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

  singleOf(::StringProvider)

  single {
    FirebaseRemoteConfig.getInstance()
  }

  // Firebase Remote Config Runner
  single<RemoteConfigRunner> {
    val firebaseRunner = FirebaseRemoteConfigProvider(
      remoteConfig = get(),
    )

    // In debug builds, wrap with DebugRemoteConfigRunner for override capability
    if (BuildConfig.DEBUG) {
      DebugRemoteConfigRunner(
        settings = get(),
        defaultRunner = firebaseRunner
      )
    } else {
      firebaseRunner
    }
  }

  singleOf(::TabletModeRemoteConfig) {
    bind<RemoteConfig>()
  }
}
