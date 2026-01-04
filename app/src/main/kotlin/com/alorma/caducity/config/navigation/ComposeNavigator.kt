package com.alorma.caducity.config.navigation

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableSharedFlow

interface ComposeNavigator<T> {
  val result: MutableSharedFlow<T>

  @Composable
  fun registerContracts()
}