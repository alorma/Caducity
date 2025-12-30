package com.alorma.caducity.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.alorma.caducity.barcode.barcodeModule
import com.alorma.caducity.base.ui.theme.LanguageManager
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.debug.AndroidDebugModeProvider
import com.alorma.caducity.debug.DebugModeProvider
import com.alorma.caducity.language.AndroidLanguageManager
import com.alorma.caducity.notification.notificationsModule
import com.alorma.caducity.version.AndroidAppVersionProvider
import com.alorma.caducity.version.AppVersionProvider
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
  includes(barcodeModule)
  includes(notificationsModule)

  single {
    Room.databaseBuilder(
      context = androidContext(),
      klass = AppDatabase::class.java,
      name = "caducity.db"
    )
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.IO)
      .build()
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
