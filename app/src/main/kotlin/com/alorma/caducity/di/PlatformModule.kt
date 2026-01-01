package com.alorma.caducity.di

import androidx.room.Room
import com.alorma.caducity.barcode.barcodeModule
import com.alorma.caducity.base.ui.theme.LanguageManager
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.debug.AndroidDebugModeProvider
import com.alorma.caducity.debug.DebugModeProvider
import com.alorma.caducity.language.AndroidLanguageManager
import com.alorma.caducity.notification.notificationsModule
import com.alorma.caducity.version.AndroidAppVersionProvider
import com.alorma.caducity.version.AppVersionProvider
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
