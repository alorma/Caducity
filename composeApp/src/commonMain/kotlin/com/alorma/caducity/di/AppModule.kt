package com.alorma.caducity.di

import com.alorma.caducity.dashboard.DashboardViewModel
import com.alorma.caducity.time.clock.AppClock
import com.alorma.caducity.time.clock.KotlinAppClock
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
  single<AppClock> { KotlinAppClock() }
  viewModelOf(::DashboardViewModel)
}
