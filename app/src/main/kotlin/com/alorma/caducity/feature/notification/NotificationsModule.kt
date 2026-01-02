package com.alorma.caducity.feature.notification

import com.alorma.caducity.feature.notification.worker.workersModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val notificationsModule: Module = module {
  includes(workersModule)

  singleOf(::AndroidExpirationNotificationHelper) {
    bind<ExpirationNotificationHelper>()
  }

  // Debug helper
  singleOf(::AndroidNotificationDebugHelper) {
    bind<NotificationDebugHelper>()
  }
}