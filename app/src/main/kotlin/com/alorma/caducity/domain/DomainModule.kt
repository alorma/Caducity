package com.alorma.caducity.domain

import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.CreateVariantUseCase
import com.alorma.caducity.domain.usecase.DeleteVariantUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.GetProductVariantsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
  factoryOf(::AppExpirationThresholdsImpl) bind ExpirationThresholds::class

  // Variant use cases
  factoryOf(::CreateVariantUseCase)
  factoryOf(::GetProductVariantsUseCase)
  factoryOf(::DeleteVariantUseCase)
}