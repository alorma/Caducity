package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.model.ProductListItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

class ObtainProductsUseCase {

  fun obtain(): Flow<ImmutableList<ProductListItem>> {
    return flow {
      emit(persistentListOf())
    }
  }

}