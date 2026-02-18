package com.alorma.caducity.feature.review

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val reviewModule = module {
  singleOf(::PlayStoreInAppReviewManager) bind InAppReviewManager::class
  singleOf(::ShowAppReviewFlag)
}
