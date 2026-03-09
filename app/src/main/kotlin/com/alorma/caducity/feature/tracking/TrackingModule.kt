package com.alorma.caducity.feature.tracking

import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for analytics tracking dependencies.
 *
 * Provides:
 * - Firebase Analytics instance
 * - Individual tracker implementations (TimberTracker, FirebaseTracker)
 * - EventTracker that delegates to all registered Tracker implementations
 */
val trackingModule =
  module {
    // Firebase Analytics instance (uses Firebase BOM for version management)
    single {
      FirebaseAnalytics.getInstance(androidContext())
    }

    // Individual tracker implementations bound to Tracker interface
    singleOf(::TimberTracker) bind Tracker::class
    singleOf(::FirebaseTracker) bind Tracker::class

    // EventTracker that automatically delegates to all registered Tracker implementations
    single {
      EventTracker(
        trackers = getAll(),
      )
    }
  }
