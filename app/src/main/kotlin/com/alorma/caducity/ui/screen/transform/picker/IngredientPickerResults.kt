package com.alorma.caducity.ui.screen.transform.picker

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** Navigation 3 has no result API: the picker leaves the final selection here and Transform collects it. */
class IngredientPickerResults {
  private val channel = Channel<List<String>>(Channel.BUFFERED)
  val results: Flow<List<String>> = channel.receiveAsFlow()

  fun deliver(itemIds: List<String>) {
    channel.trySend(itemIds)
  }
}
