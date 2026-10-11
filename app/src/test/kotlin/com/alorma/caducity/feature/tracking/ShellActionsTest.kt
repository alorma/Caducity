package com.alorma.caducity.feature.tracking

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ShellActionsTest {
  @Test
  fun `NavigateToTabAction carries the tab and shell origin`() {
    val action = NavigateToTabAction("pantry")

    expectThat(action.name).isEqualTo("nav_tab")
    expectThat(action.parameters).isEqualTo(
      mapOf("tab" to "pantry", "source" to "floating_bar", "origin" to "shell"),
    )
  }

  @Test
  fun `NavigateToAddAction comes from the fab`() {
    val action = NavigateToAddAction()

    expectThat(action.name).isEqualTo("nav_add")
    expectThat(action.parameters).isEqualTo(mapOf("source" to "fab", "origin" to "shell"))
  }
}
