package com.alorma.caducity.domain.model

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class UnitTypeTest {
  @Test
  fun `every stored name reads back to its type`() {
    UnitType.entries.forEach { expectThat(UnitType.of(it.name)).isEqualTo(it) }
  }

  @Test
  fun `an unknown or missing name is counted in units`() {
    expectThat(UnitType.of("BOTTLES")).isEqualTo(UnitType.UNITS)
    expectThat(UnitType.of(null)).isEqualTo(UnitType.UNITS)
  }

  @Test
  fun `subtypes belong to their parent`() {
    expectThat(UnitType.TUPPERS.parent).isEqualTo(UnitParent.UNITS)
    expectThat(UnitType.MILLILITRES.parent).isEqualTo(UnitParent.MEASURED)
    expectThat(UnitType.HANDFULS.parent).isEqualTo(UnitParent.HANDFULS)
  }
}
