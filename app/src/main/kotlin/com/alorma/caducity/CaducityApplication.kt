package com.alorma.caducity

import android.app.Application
import com.alorma.caducity.di.appModule
import com.alorma.caducity.feature.notification.ExpirationWorkScheduler
import com.alorma.caducity.feature.notification.NotificationChannelManager
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.debug.internal.StorageHelper
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
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

    initializeFirebase()

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

  private fun initializeFirebase() {
    // Initialize Firebase (must be done before using Firebase services)
    Firebase.initialize(this)

    // Initialize Firebase App Check
    val appCheckProvider = if (BuildConfig.DEBUG) {
      val storageHelper = StorageHelper(
        FirebaseApp.getInstance().applicationContext,
        FirebaseApp.getInstance().persistenceKey,
      )
      storageHelper.saveDebugSecret(BuildConfig.DEBUG_APP_CHECK_TOKEN)

      DebugAppCheckProviderFactory.getInstance()
    } else {
      PlayIntegrityAppCheckProviderFactory.getInstance()
    }

    Firebase.appCheck.installAppCheckProviderFactory(
      appCheckProvider
    )
  }
}
