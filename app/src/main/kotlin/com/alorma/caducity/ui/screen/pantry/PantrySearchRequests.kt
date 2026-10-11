package com.alorma.caducity.ui.screen.pantry

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** Lets Inici ask Rebost to focus its search field (P10), even if Rebost is not composed yet. */
class PantrySearchRequests {
  private val channel = Channel<Unit>(Channel.CONFLATED)
  val requests: Flow<Unit> = channel.receiveAsFlow()

  fun request() {
    channel.trySend(Unit)
  }
}
