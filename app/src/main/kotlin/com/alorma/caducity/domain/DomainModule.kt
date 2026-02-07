package com.alorma.caducity.domain

import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.GetCategoryProductsUseCase
import com.alorma.caducity.domain.usecase.PopulateFakeDataUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
  factoryOf(::AppExpirationThresholdsImpl) bind ExpirationThresholds::class

  // Product use cases (was Variant)
  factoryOf(::CreateProductUseCase)
  factoryOf(::GetCategoryProductsUseCase)

  // Debug use cases
  factoryOf(::PopulateFakeDataUseCase)
}