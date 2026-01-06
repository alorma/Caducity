package com.alorma.caducity.ui.screen.products

import com.alorma.caducity.config.ConfigQualifier
import com.alorma.caducity.domain.usecase.ObtainProductsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val productsListModule = module {
  factoryOf(::ObtainProductsUseCase)

  single {
    ProductsListMapper(
      dateFormat = get(qualifier = ConfigQualifier.DateFormat.HumanReadable),
    )
  }

  viewModelOf(::ProductsListViewModel)
}