package com.alorma.caducity.domain.model

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class UnitTypeTest {
  @Test
  fun `subtypes belong to their parent`() {
    expectThat(UnitType.TUPPERS.parent).isEqualTo(UnitParent.UNITS)
    expectThat(UnitType.MILLILITRES.parent).isEqualTo(UnitParent.MEASURED)
    expectThat(UnitType.HANDFULS.parent).isEqualTo(UnitParent.HANDFULS)
  }
}
