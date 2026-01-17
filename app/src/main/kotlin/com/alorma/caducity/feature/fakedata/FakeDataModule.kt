package com.alorma.caducity.feature.fakedata

import com.alorma.caducity.domain.usecase.GenerateFakeDataUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin DI module for fake data generation feature
 */
val fakeDataModule: Module = module {
  // Data generator
  singleOf(::FirebaseAIPromptDataSource) {
    bind<AIPromptDataSource>()
  }

  // Product matcher
  singleOf(::ProductMatcher)

  // Use case
  singleOf(::GenerateFakeDataUseCase)

  // Debug helper
  singleOf(::AndroidFakeDataDebugHelper) {
    bind<FakeDataDebugHelper>()
  }
}
