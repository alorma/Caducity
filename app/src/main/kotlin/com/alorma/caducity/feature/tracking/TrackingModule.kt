package com.alorma.caducity.feature.tracking

import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin module for tracking dependencies.
 */
val trackingModule = module {
  // Firebase Analytics instance
  single {
    FirebaseAnalytics.getInstance(androidContext())
  }

  // Individual tracker implementations
  singleOf(::TimberTracker)
  singleOf(::FirebaseTracker)

  // EventTracker that delegates to all trackers
  single {
    EventTracker(
      trackers = listOf(
        get<TimberTracker>(),
        get<FirebaseTracker>(),
      )
    )
  }
}
