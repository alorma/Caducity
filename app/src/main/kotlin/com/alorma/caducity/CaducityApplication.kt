package com.alorma.caducity

import android.app.Application
import com.alorma.caducity.config.remoteconfig.RemoteConfigProvider
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

/**
 * Custom Application class for Caducity.
 * Responsible for initializing Koin, WorkManager, notification channels, and scheduling background work.
 */
class CaducityApplication : Application() {

  private val workScheduler: ExpirationWorkScheduler by inject()
  private val remoteConfigProvider: RemoteConfigProvider by inject()
  
  // Application-scoped coroutine scope for background operations
  private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onCreate() {
    super.onCreate()

    // Initialize Timber (logging) - only in debug builds
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

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

    // Initialize Remote Config (fetch and activate in background)
    initializeRemoteConfig()
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

  private fun initializeRemoteConfig() {
    applicationScope.launch {
      remoteConfigProvider.fetchAndActivate()
        .onSuccess { activated ->
          Timber.d("Remote Config initialized successfully. New values activated: $activated")
        }
        .onFailure { exception ->
          Timber.w(exception, "Remote Config initialization failed. Using default values.")
        }
    }
  }
}
