package com.alorma.caducity.ui.components.bottomsheet

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class QuantityStateTest {
  @Test
  fun `at the minimum the decrease button is disabled and the chip is not selected`() {
    val state = QuantityState(quantity = 1, max = 4)

    expectThat(state.canDecrease).isFalse()
    expectThat(state.canIncrease).isTrue()
    expectThat(state.allSelected).isFalse()
    expectThat(state.decrease()).isEqualTo(state)
  }

  @Test
  fun `at the maximum the increase button is disabled and the chip is selected`() {
    val state = QuantityState(quantity = 4, max = 4)

    expectThat(state.canDecrease).isTrue()
    expectThat(state.canIncrease).isFalse()
    expectThat(state.allSelected).isTrue()
    expectThat(state.increase()).isEqualTo(state)
  }

  @Test
  fun `in between both buttons are enabled`() {
    val state = QuantityState(quantity = 2, max = 4)

    expectThat(state.canDecrease).isTrue()
    expectThat(state.canIncrease).isTrue()
    expectThat(state.allSelected).isFalse()
    expectThat(state.increase().quantity).isEqualTo(3)
    expectThat(state.decrease().quantity).isEqualTo(1)
  }

  @Test
  fun `the all chip sets the quantity to the pack size`() {
    expectThat(QuantityState(quantity = 1, max = 4).all().quantity).isEqualTo(4)
  }

  @Test
  fun `pips are hidden for packs larger than eight units`() {
    expectThat(QuantityState(quantity = 1, max = 8).showPips).isTrue()
    expectThat(QuantityState(quantity = 1, max = 9).showPips).isFalse()
  }
}
