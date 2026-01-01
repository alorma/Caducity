package com.alorma.caducity.domain

import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
  factoryOf(::AppExpirationThresholdsImpl) bind ExpirationThresholds::class
}