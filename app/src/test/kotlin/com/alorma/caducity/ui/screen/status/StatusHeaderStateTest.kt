package com.alorma.caducity.ui.screen.status

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class StatusHeaderStateTest {
  private val state = StatusHeaderState().apply { rangePx = 200f }
  private val connection = state.connection(LazyListState())

  @Test
  fun `a list that does not scroll never folds the header`() {
    val consumed = connection.onPreScroll(Offset(0f, -120f), NestedScrollSource.UserInput)

    expectThat(consumed).isEqualTo(Offset.Zero)
    expectThat(state.fraction).isEqualTo(0f)
  }

  @Test
  fun `scrolling down at the top of the list unfolds the header again`() {
    state.offsetPx = 200f

    val consumed = connection.onPostScroll(Offset.Zero, Offset(0f, 80f), NestedScrollSource.UserInput)

    expectThat(consumed).isEqualTo(Offset(0f, 80f))
    expectThat(state.fraction).isEqualTo(0.6f)
  }

  @Test
  fun `a folded header keeps folding first when scrolling up and never goes past the range`() {
    state.offsetPx = 150f

    val consumed = connection.onPreScroll(Offset(0f, -120f), NestedScrollSource.UserInput)

    expectThat(consumed).isEqualTo(Offset(0f, -50f))
    expectThat(state.fraction).isEqualTo(1f)
  }

  @Test
  fun `the selection mode freezes the header`() {
    state.enabled = false

    expectThat(connection.onPreScroll(Offset(0f, -50f), NestedScrollSource.UserInput)).isEqualTo(Offset.Zero)
    expectThat(
      connection.onPostScroll(Offset.Zero, Offset(0f, 50f), NestedScrollSource.UserInput),
    ).isEqualTo(Offset.Zero)
  }
}
