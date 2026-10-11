package com.alorma.caducity.ui.screen.category.quick

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** Hands the id of a category created in the quick sheet back to the screen that opened it. */
class CategoryCreatedEvents {
  private val _events =
    MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  val events: SharedFlow<String> = _events

  fun emit(categoryId: String) {
    _events.tryEmit(categoryId)
  }
}
