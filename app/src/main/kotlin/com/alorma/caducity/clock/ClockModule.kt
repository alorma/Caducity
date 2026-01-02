package com.alorma.caducity.clock

import org.koin.dsl.module

val clockModule = module {
  single<AppClock> { KotlinAppClock() }
}