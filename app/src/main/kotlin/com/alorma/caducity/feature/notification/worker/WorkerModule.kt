package com.alorma.caducity.feature.notification.worker

import com.alorma.caducity.feature.ai.ModelDownloadWorker
import com.alorma.caducity.feature.notification.ExpirationWorkScheduler
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val workersModule =
  module {
    workerOf(::ExpirationCheckWorker)
    workerOf(::ModelDownloadWorker)

    singleOf(::NotificationDelayCalculator)

    singleOf(::ExpirationWorkSchedulerImpl) {
      bind<ExpirationWorkScheduler>()
    }
  }
