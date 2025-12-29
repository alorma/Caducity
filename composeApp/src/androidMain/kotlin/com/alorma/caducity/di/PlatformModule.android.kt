package com.alorma.caducity.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.debug.AndroidDebugModeProvider
import com.alorma.caducity.debug.DebugModeProvider
import com.alorma.caducity.notification.AndroidExpirationNotificationHelper
import com.alorma.caducity.notification.AndroidNotificationDebugHelper
import com.alorma.caducity.notification.ExpirationNotificationHelper
import com.alorma.caducity.notification.ExpirationWorkScheduler
import com.alorma.caducity.notification.NotificationDebugHelper
import com.alorma.caducity.version.AndroidAppVersionProvider
import com.alorma.caducity.version.AppVersionProvider
import com.alorma.caducity.worker.ExpirationCheckWorker
import com.alorma.caducity.worker.ExpirationWorkSchedulerImpl
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
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
}
