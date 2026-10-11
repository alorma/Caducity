package com.alorma.caducity.domain

import com.alorma.caducity.domain.usecase.AddItemBatchUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.GetCategoryProductsUseCase
import com.alorma.caducity.domain.usecase.GetNameSuggestionsUseCase
import com.alorma.caducity.domain.usecase.MoveItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.PopulateFakeDataUseCase
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
    factoryOf(::CreateProductUseCase)
    factoryOf(::GetCategoryProductsUseCase)

    // Redesign use cases
    factoryOf(::AddItemBatchUseCase)
    factoryOf(::UndoAddItemsUseCase)
    factoryOf(::DiscardItemsUseCase)
    factoryOf(::RestoreItemsUseCase)
    factoryOf(::UpdateItemUseCase)
    factoryOf(::MoveItemToCategoryUseCase)
    factoryOf(::GetNameSuggestionsUseCase)
    factoryOf(::UpdateCategoryUseCase)
    factoryOf(::SeedCategoriesUseCase)

    // Debug use cases
    factoryOf(::PopulateFakeDataUseCase)

    // Fake data strategies
    factoryOf(::FakeTestDataStrategy)
    factoryOf(::FakePlayStoreDataStrategy)
  }
