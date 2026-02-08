package com.alorma.caducity.feature.consent

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin module for consent management dependencies.
 *
 * Provides:
 * - ConsentManager for managing user consent preferences
 * - ConsentFlag for tracking whether user has been shown consent screen
 */
val consentModule = module {
  singleOf(::ConsentManager)
  singleOf(::ConsentFlag)
}
