package com.alorma.caducity

import android.app.Application
import com.alorma.caducity.di.appModule
import com.alorma.caducity.notification.NotificationChannelManager
import com.alorma.caducity.worker.ExpirationWorkScheduler
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Custom Application class for Caducity.
 * Responsible for initializing Koin, WorkManager, notification channels, and scheduling background work.
 */
class CaducityApplication : Application() {

  private val workScheduler: ExpirationWorkScheduler by inject()

  override fun onCreate() {
    super.onCreate()

    // Initialize Koin
    startKoin {
      androidLogger(Level.ERROR) // Only log errors in production
      androidContext(this@CaducityApplication)
      workManagerFactory() // Enable Koin WorkManager integration
      modules(appModule)
    }

    // Create notification channels
    NotificationChannelManager.createNotificationChannels(this)

    // Schedule expiration check work
    workScheduler.scheduleExpirationCheck()
  }
}
