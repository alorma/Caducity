package com.alorma.caducity.di

import androidx.room.Room
import com.alorma.caducity.feature.barcode.barcodeModule
import com.alorma.caducity.config.language.LanguageManager
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.feature.debug.AndroidDebugModeProvider
import com.alorma.caducity.feature.debug.DebugModeProvider
import com.alorma.caducity.config.language.AndroidLanguageManager
import com.alorma.caducity.feature.notification.notificationsModule
import com.alorma.caducity.config.version.AndroidAppVersionProvider
import com.alorma.caducity.feature.version.AppVersionProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformModule: Module = module {
  includes(barcodeModule)
  includes(notificationsModule)

  single {
    Room.databaseBuilder(
      androidContext(),
      AppDatabase::class.java,
      "caducity.db"
    ).build()
  }

  singleOf(::AndroidAppVersionProvider) {
    bind<AppVersionProvider>()
  }

  singleOf(::AndroidDebugModeProvider) {
    bind<DebugModeProvider>()
  }

  singleOf(::AndroidLanguageManager) {
    bind<LanguageManager>()
  }
}
