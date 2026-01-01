package com.alorma.caducity.worker

import com.alorma.caducity.notification.ExpirationWorkScheduler
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val workersModule = module {
  workerOf(::ExpirationCheckWorker)

  singleOf(::ExpirationWorkSchedulerImpl) {
    bind<ExpirationWorkScheduler>()
  }
}