package com.alorma.caducity.domain

import com.alorma.caducity.domain.usecase.AddItemBatchUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.GetActiveItemUseCase
import com.alorma.caducity.domain.usecase.GetNameSuggestionsUseCase
import com.alorma.caducity.domain.usecase.MoveItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveCalendarUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveHomeUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsByStatusUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.ObservePantryUseCase
import com.alorma.caducity.domain.usecase.ObserveWeekStripUseCase
import com.alorma.caducity.domain.usecase.PopulateFakeDataUseCase
import com.alorma.caducity.domain.usecase.RemoveBatchEntryUseCase
import com.alorma.caducity.domain.usecase.RestoreBatchUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.domain.usecase.SeedCategoriesUseCase
import com.alorma.caducity.domain.usecase.UndoAddItemsUseCase
import com.alorma.caducity.domain.usecase.UpdateCategoryUseCase
import com.alorma.caducity.domain.usecase.UpdateItemUseCase
import com.alorma.caducity.domain.usecase.fakedata.FakePlayStoreDataStrategy
import com.alorma.caducity.domain.usecase.fakedata.FakeTestDataStrategy
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule =
  module {
    factoryOf(::AppExpirationThresholdsImpl) bind ExpirationThresholds::class

    // Product use cases (was Variant)

    // Redesign use cases
    factoryOf(::AddItemBatchUseCase)
    factoryOf(::UndoAddItemsUseCase)
    factoryOf(::DiscardItemsUseCase)
    factoryOf(::RestoreItemsUseCase)
    factoryOf(::UpdateItemUseCase)
    factoryOf(::MoveItemToCategoryUseCase)
    factoryOf(::GetNameSuggestionsUseCase)
    factoryOf(::ObserveCategoriesUseCase)
    factoryOf(::GetActiveItemUseCase)
    factoryOf(::RemoveBatchEntryUseCase)
    factoryOf(::RestoreBatchUseCase)
    factoryOf(::UpdateCategoryUseCase)
    factoryOf(::SeedCategoriesUseCase)
    factoryOf(::ObserveCalendarUseCase)
    factoryOf(::ObserveWeekStripUseCase)
    factoryOf(::ObserveCategoryUseCase)
    factoryOf(::ObserveItemsByStatusUseCase)
    factoryOf(::ObserveHomeUseCase)
    factoryOf(::ObserveItemsUseCase)
    factoryOf(::ObservePantryUseCase)

    // Debug use cases
    factoryOf(::PopulateFakeDataUseCase)

    // Fake data strategies
    factoryOf(::FakeTestDataStrategy)
    factoryOf(::FakePlayStoreDataStrategy)
  }
